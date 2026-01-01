"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {isPlatformAuthenticatorAvailableSafe,isWebAuthnSupported} from "@/app/lib/passkey"



export default function PostAuthPage() {
  const router = useRouter();
  const sp = useSearchParams();

  useEffect(() => {
    (async () => {
      const next = sp.get("next") || "/dashboard";

      // 你自己的“是否已经注册 passkey”的判断：
      // 推荐：调用后端 /webauthn/credentials/me（或 /me/security）
      // 这里先用 localStorage 占位
      const already = localStorage.getItem("ledgerx_passkey_registered") === "true";

      if (!already && (await isWebAuthnSupported()) && (await isPlatformAuthenticatorAvailableSafe())) {
        router.replace(`/passkey/nudge?next=${encodeURIComponent(next)}`);
        return;
      }

      router.replace(next);
    })();
  }, [router, sp]);

  return null;
}
