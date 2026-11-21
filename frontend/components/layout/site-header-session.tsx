"use client";

import { useEffect, useState } from "react";

import { SiteHeader } from "@/components/layout/site-header";
import { getSession } from "@/features/auth/services/auth.service";
import {
  SESSION_EVENT,
  sessionInitials,
  sessionLabel,
  type SessionUser,
} from "@/lib/auth/session";

/**
 * Client-side session boundary for the public header.
 * Keeping the request here lets the public layout stay statically prerenderable
 * while the chip still follows real logins and logouts.
 */
export function SiteHeaderSession() {
  const [user, setUser] = useState<SessionUser | null>(null);

  useEffect(() => {
    let active = true;

    const load = () => {
      void getSession()
        .then((session) => {
          if (active) {
            setUser(session.user);
          }
        })
        .catch(() => {
          if (active) {
            setUser(null);
          }
        });
    };

    load();
    window.addEventListener(SESSION_EVENT, load);

    return () => {
      active = false;
      window.removeEventListener(SESSION_EVENT, load);
    };
  }, []);

  return (
    <SiteHeader
      account={
        user
          ? { initials: sessionInitials(user), label: sessionLabel(user) }
          : undefined
      }
    />
  );
}
