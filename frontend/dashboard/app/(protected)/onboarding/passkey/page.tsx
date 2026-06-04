'use client'
import React, {useEffect, useMemo, useState} from "react";
import {isWebAuthnSupported, isPlatformAuthenticatorAvailableSafe} from "@/app/lib/passkey"
import {apiPost} from "@/app/lib/api";
import {
    toPublicKeyCredentialCreationOptions,
    cleanPublicKeyCreationOptions
} from "@/app/features/passkey/utils/webauthn"
import type {PublicKeyCredentialCreationOptionsJSON} from "@/app/features/passkey/types";
import {credentialToJson} from "@/app/features/passkey/utils/webauthn";
import {useRouter} from "next/navigation";


/**
 * LedgerX - Passkey Nudge Page
 * - Detect WebAuthn support
 * - Let user decide: Register now / Later / Don't remind
 */

const LS_KEY = "ledgerx_passkey_nudge_choice"; // "later" | "dismissed" | "done"
const LS_TS = "ledgerx_passkey_nudge_ts"; // last shown timestamp


/**
 * You can call this from login-success flow too.
 * Example policy:
 * - show if not dismissed and not done
 * - show at most once per 7 days (if user chose "later")
 */
function shouldShowNudge() {
    const choice = localStorage.getItem(LS_KEY);
    if (choice === "dismissed" || choice === "done") return false;

    if (choice === "later") {
        const lastTs = Number(localStorage.getItem(LS_TS) || "0");
        const sevenDays = 7 * 24 * 60 * 60 * 1000;
        return Date.now() - lastTs >= sevenDays;
    }

    return true; // first time
}

export default function PasskeyNudgePage() {
    const router = useRouter();

    const [supported, setSupported] = useState(false);
    const [platformAvailable, setPlatformAvailable] = useState(false);
    const [checking, setChecking] = useState(true);

    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    const canOfferPasskey = useMemo(() => supported && platformAvailable, [supported, platformAvailable]);

    useEffect(() => {
        (async () => {
            setChecking(true);
            const sup = isWebAuthnSupported();
            setSupported(sup);

            const pa = await isPlatformAuthenticatorAvailableSafe();
            setPlatformAvailable(pa);

            setChecking(false);

            // record last shown
            try {
                localStorage.setItem(LS_TS, String(Date.now()));
            } catch {
            }
        })();
    }, []);

    useEffect(() => {
        // If you use it as a standalone route, you can optionally redirect away if no need to show.
        try {
            if (!shouldShowNudge()) {
                // e.g. window.location.href = "/settings/security";
                // For now, do nothing.
            }
        } catch {
        }
    }, []);

    const handleLater = () => {
        try {
            localStorage.setItem(LS_KEY, "later");
            localStorage.setItem(LS_TS, String(Date.now()));
        } catch {
        }
        // Navigate away
        // window.location.href = "/dashboard";
        alert("好的～你可以之后在「设置 > 安全」里随时开启 Passkey。");
    };

    const handleDismiss = () => {
        try {
            localStorage.setItem(LS_KEY, "dismissed");
            localStorage.setItem(LS_TS, String(Date.now()));
        } catch {
        }
        alert("已不再提醒。你仍可在「设置 > 安全」里开启 Passkey。");
    };

    /**
     * Replace this with your real WebAuthn flow:
     * 1) POST /webauthn/register/options -> PublicKeyCredentialCreationOptions (JSON)
     * 2) navigator.credentials.create({ publicKey: ... })
     * 3) POST /webauthn/register/finish -> send attestation response
     */
    const startPasskeyRegistration = async () => {
        setError("");
        setBusy(true);
        try {
            const resp = await apiPost<any>("/api/passkey/registration/options", {})
            console.log("raw resp.extensions", resp?.extensions);
            if (resp?.extensions && "appidExclude" in resp.extensions) {
                delete resp.extensions.appidExclude;
            }
            const rawPK = toPublicKeyCredentialCreationOptions(resp as PublicKeyCredentialCreationOptionsJSON);
            const publicKey = cleanPublicKeyCreationOptions(rawPK)


            const credential = await navigator.credentials.create({
                publicKey: publicKey
            });
            if (!credential || !(credential instanceof PublicKeyCredential)) {
                throw new Error("Failed to create credential");
            }
            const attestation = credentialToJson(credential);
            await apiPost("/api/passkey/registration/finish", attestation)
            localStorage.setItem(LS_KEY, "done");

            alert("Passkey 注册完成！（这里是示例，请接入真实 WebAuthn 流程）");
            router.push("/dashboard");
            // window.location.href = "/dashboard";
        } catch (e: any) {
            console.error("webauthn error raw:", e);
            console.error("name:", e?.name, "message:", e?.message);
            console.error("stack:", e?.stack);
            setError(e?.message || "注册失败，请稍后重试。");
        } finally {
            setBusy(false);
        }
    };

    return (
        <div style={styles.page}>
            <div style={styles.card}>
                <div style={styles.header}>
                    <div style={styles.badge}>🔐 Passkey</div>
                    <h1 style={styles.title}>用 Passkey 让登录更快、更安全</h1>
                    <p style={styles.subtitle}>
                        之后你可以用 Face ID / Touch ID / Windows Hello 直接登录，无需记密码，也更不容易被钓鱼网站骗到。
                    </p>
                </div>

                <div style={styles.section}>
                    <h2 style={styles.h2}>你将获得</h2>
                    <ul style={styles.ul}>
                        <li>✅ 更快：一触即登，不用输入密码/验证码</li>
                        <li>✅ 更安全：抗钓鱼，减少撞库风险</li>
                        <li>✅ 多设备：可同步到你的设备生态（取决于平台）</li>
                    </ul>
                </div>

                <div style={styles.section}>
                    <h2 style={styles.h2}>温馨提示</h2>
                    <ul style={styles.ul}>
                        <li>你随时可以在「设置 &gt; 安全」里添加/撤销 Passkey</li>
                        <li>建议保留至少一种备用登录方式（邮箱/手机）</li>
                    </ul>
                </div>

                <div style={styles.section}>
                    {checking ? (
                        <div style={styles.notice}>正在检测当前设备是否支持 Passkey…</div>
                    ) : canOfferPasskey ? (
                        <div style={{...styles.notice, ...styles.noticeOk}}>
                            当前设备支持 Passkey ✅（可使用平台认证器）
                        </div>
                    ) : (
                        <div style={{...styles.notice, ...styles.noticeWarn}}>
                            当前环境暂不支持 Passkey（或未检测到平台认证器）。你仍可稍后在支持的设备上开启。
                        </div>
                    )}
                </div>

                {error ? <div style={styles.error}>⚠️ {error}</div> : null}

                <div style={styles.actions}>
                    <button
                        style={{...styles.primaryBtn, opacity: canOfferPasskey && !busy ? 1 : 0.6}}
                        disabled={!canOfferPasskey || busy}
                        onClick={startPasskeyRegistration}
                    >
                        {busy ? "正在开启…" : "立即开启 Passkey"}
                    </button>

                    <button style={styles.secondaryBtn} disabled={busy} onClick={handleLater}>
                        稍后再说
                    </button>

                    <button style={styles.linkBtn} disabled={busy} onClick={handleDismiss}>
                        不再提醒
                    </button>
                </div>

                <div style={styles.footer}>
          <span style={styles.small}>
            你也可以直接去 <b>设置 &gt; 安全</b> 管理 Passkey
          </span>
                </div>
            </div>
        </div>
    );
}

const styles = {
    page: {
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        background: "linear-gradient(180deg, #0b1020 0%, #0a0f1a 60%, #070b12 100%)",
        padding: 20,
        color: "#e8eefc",
        fontFamily:
            'ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Apple Color Emoji","Segoe UI Emoji"',
    },
    card: {
        width: "100%",
        maxWidth: 720,
        borderRadius: 18,
        background: "rgba(255,255,255,0.06)",
        border: "1px solid rgba(255,255,255,0.12)",
        boxShadow: "0 10px 30px rgba(0,0,0,0.35)",
        padding: 24,
        backdropFilter: "blur(10px)",
    },
    header: {marginBottom: 18},
    badge: {
        display: "inline-block",
        padding: "6px 10px",
        borderRadius: 999,
        background: "rgba(255,255,255,0.10)",
        border: "1px solid rgba(255,255,255,0.14)",
        fontSize: 12,
        letterSpacing: 0.2,
        marginBottom: 10,
    },
    title: {margin: "6px 0 8px", fontSize: 26, lineHeight: 1.2},
    subtitle: {margin: 0, opacity: 0.85, lineHeight: 1.55, fontSize: 14},
    section: {marginTop: 16},
    h2: {fontSize: 15, margin: "0 0 10px", opacity: 0.95},
    ul: {margin: 0, paddingLeft: 18, lineHeight: 1.8, opacity: 0.92, fontSize: 14},
    notice: {
        padding: 12,
        borderRadius: 12,
        background: "rgba(255,255,255,0.07)",
        border: "1px solid rgba(255,255,255,0.10)",
        fontSize: 13,
        opacity: 0.95,
    },
    noticeOk: {
        background: "rgba(46, 204, 113, 0.12)",
        border: "1px solid rgba(46, 204, 113, 0.30)",
    },
    noticeWarn: {
        background: "rgba(241, 196, 15, 0.12)",
        border: "1px solid rgba(241, 196, 15, 0.30)",
    },
    error: {
        marginTop: 12,
        padding: 12,
        borderRadius: 12,
        background: "rgba(231, 76, 60, 0.12)",
        border: "1px solid rgba(231, 76, 60, 0.30)",
        fontSize: 13,
    },
    actions: {
        display: "flex",
        gap: 10,
        alignItems: "center",
        flexWrap: "wrap" as const,
        marginTop: 18,
    },
    primaryBtn: {
        padding: "10px 14px",
        borderRadius: 12,
        border: "1px solid rgba(255,255,255,0.16)",
        background: "rgba(99, 102, 241, 0.55)",
        color: "#fff",
        cursor: "pointer",
        fontWeight: 600,
    },
    secondaryBtn: {
        padding: "10px 14px",
        borderRadius: 12,
        border: "1px solid rgba(255,255,255,0.16)",
        background: "rgba(255,255,255,0.08)",
        color: "#fff",
        cursor: "pointer",
        fontWeight: 600,
    },
    linkBtn: {
        padding: "10px 6px",
        border: "none",
        background: "transparent",
        color: "rgba(232,238,252,0.75)",
        cursor: "pointer",
        textDecoration: "underline",
    },
    footer: {marginTop: 14, opacity: 0.8},
    small: {fontSize: 12},
};
