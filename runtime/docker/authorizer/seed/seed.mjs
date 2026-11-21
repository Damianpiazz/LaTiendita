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
    body: {
      email: account.email,
      password: PASSWORD,
      confirm_password: PASSWORD,
      given_name: account.givenName,
      family_name: account.familyName,
      roles: ["cliente"],
    },
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

  let ready = 0;

  for (const [index, account] of accounts.entries()) {
    try {
      const state = await seedAccount(account);
      ready += 1;
      console.log(`  ${account.email}  ${state}`);
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
