#!/usr/bin/env node
/**
 * Seeds Authorizer itself, through its public v1 API. Runs as a one-shot
 * container started by Docker Compose, with no frontend or gateway involved.
 *
 * Idempotent: an account that can log in is reused; a missing one is created.
 * No password, token or MFA cookie is ever printed.
 */

import { readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const accounts = JSON.parse(readFileSync(join(here, "accounts.json"), "utf8"));

const AUTHORIZER_URL = (process.env.AUTHORIZER_URL ?? "http://authorizer:8080").replace(/\/$/, "");
const ORIGIN = process.env.SEED_ORIGIN ?? "http://localhost:3000";
const PASSWORD = process.env.SEED_USER_PASSWORD ?? "Password123!";
const ADMIN_SECRET = process.env.AUTHORIZER_ADMIN_SECRET ?? "";
const TIMEOUT_MS = Number(process.env.SEED_TIMEOUT_MS ?? 10000);
const ATTEMPTS = Number(process.env.SEED_ATTEMPTS ?? 3);

function isMfaOffer(payload) {
  if (!payload || typeof payload !== "object") {
    return false;
  }

  const token = typeof payload.access_token === "string" ? payload.access_token.trim() : "";
  if (token) {
    return false;
  }

  return (
    payload.should_show_totp_screen === true ||
    payload.should_show_email_otp_screen === true ||
    payload.should_show_mobile_otp_screen === true ||
    String(payload.message ?? "").toLowerCase().includes("mfa")
  );
}

function mfaCookieHeader(setCookies) {
  const parts = [];

  for (const value of Array.isArray(setCookies) ? setCookies : [setCookies]) {
    if (typeof value !== "string") {
      continue;
    }

    const match = value.match(/(?:^|;\s*)(mfa_session)=([^;]*)/);
    if (match) {
      parts.push(`${match[1]}=${match[2]}`);
    }
  }

  return parts.length ? parts.join("; ") : undefined;
}

function cookieHeader(setCookies, name) {
  const parts = [];

  for (const value of Array.isArray(setCookies) ? setCookies : [setCookies]) {
    if (typeof value !== "string") {
      continue;
    }

    const match = value.match(new RegExp(`(?:^|;\\s*)${name}=([^;]*)`));
    if (match) {
      parts.push(`${name}=${match[1]}`);
    }
  }

  return parts.length ? parts.join("; ") : undefined;
}

/**
 * Authorizer has no admin endpoint that overwrites a password in place. When a
 * seeded email already exists with a stale password, the only supported reset
 * is to delete the user and sign them up again. That changes the Authorizer
 * `sub`, which customer-service absorbs by reclaiming the orphaned row by email.
 */
async function adminSession() {
  if (!ADMIN_SECRET) {
    throw new Error("AUTHORIZER_ADMIN_SECRET is required to reclaim an existing account");
  }

  const login = await request("/v1/admin/login", {
    method: "POST",
    body: { admin_secret: ADMIN_SECRET },
  });

  const cookie = cookieHeader(login.setCookies, "authorizer-admin");
  if (login.status >= 400 || !cookie) {
    throw new Error(`admin login failed with status ${login.status}`);
  }

  return cookie;
}

async function deleteUserByEmail(cookie, email) {
  const lookup = await request("/v1/admin/user", {
    method: "POST",
    body: { email },
    cookie,
  });

  // Authorizer wraps the user in { user: {...} } on this endpoint.
  const userId = lookup.data?.user?.id ?? lookup.data?.id;
  if (lookup.status >= 400 || !userId) {
    return false;
  }

  const removal = await request("/v1/admin/delete_user", {
    method: "POST",
    body: { id: userId },
    cookie,
  });

  if (removal.status >= 400) {
    throw new Error(`admin delete_user failed with status ${removal.status}`);
  }

  return true;
}

function desiredRoles(account) {
  return Array.isArray(account.roles) && account.roles.length > 0
    ? account.roles
    : ["cliente"];
}

async function ensureRoles(adminCookie, account) {
  const roles = desiredRoles(account);
  const lookup = await request("/v1/admin/user", {
    method: "POST",
    body: { email: account.email },
    cookie: adminCookie,
  });

  const user = lookup.data?.user ?? lookup.data;
  const userId = user?.id;
  if (lookup.status >= 400 || !userId) {
    throw new Error(`admin lookup failed with status ${lookup.status}`);
  }

  const currentRoles = Array.isArray(user.roles) ? [...user.roles].sort() : [];
  const wantedRoles = [...roles].sort();
  if (JSON.stringify(currentRoles) === JSON.stringify(wantedRoles)) {
    return false;
  }

  const update = await request("/v1/admin/update_user", {
    method: "POST",
    body: { id: userId, roles },
    cookie: adminCookie,
  });

  if (update.status >= 400) {
    throw new Error(`admin update_user failed with status ${update.status}`);
  }

  return true;
}

async function request(path, { method = "GET", body, cookie } = {}) {
  const headers = { Accept: "application/json", Origin: ORIGIN };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (cookie) {
    headers.Cookie = cookie;
  }

  for (let attempt = 1; attempt <= ATTEMPTS; attempt += 1) {
    try {
      const response = await fetch(`${AUTHORIZER_URL}${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: AbortSignal.timeout(TIMEOUT_MS),
      });
      const text = await response.text();
      let data;

      try {
        data = text ? JSON.parse(text) : undefined;
      } catch {
        data = undefined;
      }

      const setCookies =
        typeof response.headers.getSetCookie === "function"
          ? response.headers.getSetCookie()
          : [];

      if (response.status >= 500 && attempt < ATTEMPTS) {
        await new Promise((resolve) => setTimeout(resolve, 250 * attempt));
        continue;
      }

      return { status: response.status, data, setCookies };
    } catch {
      if (attempt === ATTEMPTS) {
        throw new Error(`request to ${path} failed after ${ATTEMPTS} attempts`);
      }

      await new Promise((resolve) => setTimeout(resolve, 250 * attempt));
    }
  }

  throw new Error(`request to ${path} failed`);
}

async function skipMfa(email, setCookies) {
  const cookie = mfaCookieHeader(setCookies);
  if (!cookie) {
    throw new Error("MFA offer arrived without an mfa_session cookie");
  }

  const response = await request("/v1/skip_mfa_setup", {
    method: "POST",
    body: { email },
    cookie,
  });

  if (response.status >= 400 || !response.data?.access_token) {
    throw new Error(`MFA skip failed with status ${response.status}`);
  }

  return true;
}

function signupBody(account) {
  return {
    email: account.email,
    password: PASSWORD,
    confirm_password: PASSWORD,
    given_name: account.givenName,
    family_name: account.familyName,
    roles: ["cliente"],
  };
}

async function seedAccount(account) {
  const login = await request("/v1/login", {
    method: "POST",
    body: { email: account.email, password: PASSWORD },
  });

  if (isMfaOffer(login.data)) {
    await skipMfa(account.email, login.setCookies);
    return "existing";
  }

  if (login.status < 400 && login.data?.access_token) {
    return "existing";
  }

  // Only a genuine credential miss means "create it". A 429/5xx must not be
  // mistaken for a missing account.
  if (login.status !== 401 && login.status !== 404) {
    throw new Error(`login failed with status ${login.status}`);
  }

  const signup = await request("/v1/signup", {
    method: "POST",
    body: signupBody(account),
  });

  if (signup.status >= 400) {
    // A prior run may have created the account between login and signup.
    const retry = await request("/v1/login", {
      method: "POST",
      body: { email: account.email, password: PASSWORD },
    });

    if (retry.data?.access_token || isMfaOffer(retry.data)) {
      return "existing";
    }

    // The email exists but the password no longer matches. Authorizer cannot
    // overwrite a password in place: reclaim the user through the admin API
    // and recreate it with the configured password.
    const admin = await adminSession();
    if (await deleteUserByEmail(admin, account.email)) {
      const recreated = await request("/v1/signup", {
        method: "POST",
        body: signupBody(account),
      });

      if (recreated.status >= 400) {
        throw new Error(`signup after reclaim failed with status ${recreated.status}`);
      }

      if (isMfaOffer(recreated.data)) {
        await skipMfa(account.email, recreated.setCookies);
      }

      return "reclaimed";
    }

    throw new Error(`signup failed with status ${signup.status}`);
  }

  if (isMfaOffer(signup.data)) {
    await skipMfa(account.email, signup.setCookies);
  }

  return "created";
}

async function main() {
  console.log(`Authorizer seed -> ${AUTHORIZER_URL}`);
  console.log(`  accounts: ${accounts.length}`);

  const adminCookie = await adminSession();
  let ready = 0;

  for (const [index, account] of accounts.entries()) {
    try {
      const state = await seedAccount(account);
      const rolesUpdated = await ensureRoles(adminCookie, account);
      ready += 1;
      const roleLabel = rolesUpdated ? `, roles -> ${desiredRoles(account).join(",")}` : "";
      console.log(`  ${account.email}  ${state}${roleLabel}`);
    } catch (error) {
      const reason = error instanceof Error ? error.message : "unknown error";
      console.error(`  FAILED ${account.email}: ${reason}`);
    }

    if (index < accounts.length - 1) {
      await new Promise((resolve) => setTimeout(resolve, 300));
    }
  }

  console.log(`  ${ready}/${accounts.length} ready`);

  if (ready !== accounts.length) {
    process.exitCode = 1;
  }
}

await main();
