'use client';

import { useEffect, useMemo, useState } from 'react';
import { OtpCtx, verifyOtp, resendOtp } from '../../lib/auth';
import { useRouter } from 'next/navigation';
import { setDefaultResultOrder } from 'dns';
import {isPlatformAuthenticatorAvailableSafe} from "@/app/lib/passkey"

function readCtx(): OtpCtx | null {
  try {
    const raw = sessionStorage.getItem('otp_ctx');
    return raw ? (JSON.parse(raw) as OtpCtx) : null;
  } catch (e) {
    return null;
  }
}

export default function VerifyOtpPage() {
  const router = useRouter();
  const [ctx, setCtx] = useState<OtpCtx | null>(null);
  const [code, setCode] = useState('');
  const [msg, setMsg] = useState('');
  const [busy, setBusy] = useState(false);
  const [cooldown, setCooldown] = useState(0);

  useEffect(() => {
    const c = readCtx();
    if (!c) return router.replace('/');
    setCtx(c);
    setCooldown(c.cooldownSeconds);
  }, [router]);

  useEffect(() => {
    if (cooldown <= 0) {
      return;
    }
    const t = setInterval(() => setCooldown((x) => x - 1), 1000);
    return () => clearInterval(t);
  }, [cooldown]);

  const title = useMemo(() => {
    if (!ctx) return 'Verify';
    return ctx.identityType === 'EMAIL' ? 'Verify Email' : 'Verify Phone';
  }, [ctx]);

  async function onVerify() {
    if (!ctx) return;
    setMsg('');
    const c = code.trim();
    if (!/^\d{6}$/.test(c)) {
      setMsg('Please enter a 6-digit code');
      return;
    }
    setBusy(true);
    try {
      const r = await verifyOtp({
        identityType: ctx.identityType,
        identity: ctx.normalizedIdentity,
        code: c,
      });
      if(!r.verified){
        setMsg(r.errorCode);
        return;
      }
      if (r.next === 'SIGNED_IN') {
        sessionStorage.removeItem('otp_ctx');
        sessionStorage.setItem('signup_token',r.signupToken)
        if (!r.hasPasskey && await isPlatformAuthenticatorAvailableSafe()) {
          router.replace("/onboarding/passkey");
        } else {
          router.replace("/dashboard");
        }
        return;
      }
      if (r.next === 'CREATE_ACCOUNT') {
        sessionStorage.removeItem('otp_ctx');
        sessionStorage.setItem('signup_token',r.signupToken)
        if (r.signupToken) sessionStorage.setItem('signupToken', r.signupToken);
        if (!r.hasPasskey && await isPlatformAuthenticatorAvailableSafe()) {
          router.replace("/passkey/nudge?next=/dashboard");
        } else {
          router.replace("/dashboard");
        }
        return;
      }
      setMsg('Unexpected error');
    } catch (e) {
      setMsg(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function onResend() {
    if (!ctx) return;
    if (cooldown > 0) return;
    setMsg('');
    setBusy(true);
    try {
      const r = await resendOtp({
        identityType: ctx.identityType,
        identity: ctx.normalizedIdentity,
      });
      setCooldown(r.cooldownSeconds);
      setMsg('Code sent.');
    } catch (e: any) {
      setMsg(e?.message ?? String(e));
    } finally {
      setBusy(false);
    }
  }

  if (!ctx) return null;

  return (
    <div
      style={{
        minHeight: '100vh',
        background: '#f5f5f5',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1rem',
      }}
    >
      <div style={{ maxWidth: '400px', width: '100%' }}>
        {/* 标题 */}
        <h1
          style={{
            fontSize: '1.875rem',
            fontWeight: '600',
            color: '#374151',
            marginBottom: '2rem',
            textAlign: 'center',
          }}
        >
          {title}
        </h1>

        {/* 验证卡片 */}
        <div
          style={{
            backgroundColor: 'white',
            borderRadius: '0.5rem',
            padding: '1.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          }}
        >
          {/* 提示信息 */}
          <p
            style={{
              fontSize: '0.875rem',
              color: '#6b7280',
              marginBottom: '1.5rem',
              textAlign: 'center',
            }}
          >
            We sent a 6-digit code to <span style={{ fontWeight: '500', color: '#374151' }}>{ctx.maskedIdentity}</span>
          </p>

          {/* 验证码输入框 */}
          <div style={{ marginBottom: '1rem' }}>
            <label
              htmlFor="otp-code"
              style={{
                display: 'block',
                fontSize: '0.875rem',
                fontWeight: '500',
                color: '#374151',
                marginBottom: '0.5rem',
              }}
            >
              Verification code
            </label>
            <input
              id="otp-code"
              type="text"
              value={code}
              onChange={(e) => {
                const value = e.target.value.replace(/\D/g, '').slice(0, 6);
                setCode(value);
                setMsg('');
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !busy && code.length === 6) {
                  onVerify();
                }
              }}
              inputMode="numeric"
              placeholder="123456"
              maxLength={6}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                outline: 'none',
                fontSize: '1.25rem',
                textAlign: 'center',
                letterSpacing: '0.1em',
                fontFamily: 'monospace',
                boxSizing: 'border-box',
              }}
            />
          </div>

          {/* 错误/成功提示 */}
          {msg && (
            <div
              style={{
                backgroundColor: msg === 'Code sent.' ? '#f0fdf4' : '#fef2f2',
                border: `1px solid ${msg === 'Code sent.' ? '#bbf7d0' : '#fecaca'}`,
                borderRadius: '0.375rem',
                padding: '0.75rem',
                marginBottom: '1rem',
              }}
            >
              <p
                style={{
                  color: msg === 'Code sent.' ? '#16a34a' : '#dc2626',
                  fontSize: '0.875rem',
                  margin: 0,
                }}
              >
                {msg}
              </p>
            </div>
          )}

          {/* Verify 按钮 */}
          <button
            onClick={onVerify}
            disabled={busy || code.length !== 6}
            style={{
              width: '100%',
              backgroundColor: busy || code.length !== 6 ? '#9ca3af' : '#2563eb',
              color: 'white',
              fontWeight: '500',
              padding: '0.75rem',
              borderRadius: '0.375rem',
              border: 'none',
              cursor: busy || code.length !== 6 ? 'not-allowed' : 'pointer',
              fontSize: '0.875rem',
              marginBottom: '0.75rem',
            }}
          >
            {busy ? 'Verifying...' : 'Verify'}
          </button>

          {/* Resend 按钮 */}
          <button
            onClick={onResend}
            disabled={busy || cooldown > 0}
            style={{
              width: '100%',
              backgroundColor: 'white',
              color: cooldown > 0 ? '#9ca3af' : '#2563eb',
              fontWeight: '500',
              padding: '0.75rem',
              borderRadius: '0.375rem',
              border: '1px solid #d1d5db',
              cursor: busy || cooldown > 0 ? 'not-allowed' : 'pointer',
              fontSize: '0.875rem',
            }}
          >
            {cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend code'}
          </button>
        </div>

        {/* 底部链接 */}
        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
          <a
            href="/"
            style={{
              color: '#2563eb',
              textDecoration: 'underline',
              fontSize: '0.875rem',
            }}
          >
            Use a different email/phone
          </a>
        </div>
      </div>
    </div>
  );
}
