import type { PublicKeyCredentialCreationOptionsJSON, PublicKeyCredentialRequestOptionsJSON,AuthenticatorTransport } from '../types';

export function toPublicKeyCredentialCreationOptions(
    json: PublicKeyCredentialCreationOptionsJSON
  ): PublicKeyCredentialCreationOptions {
    return {
      ...json,
      challenge: base64urlToArrayBuffer(json.challenge),
      user: {
        ...json.user,
        id: base64urlToArrayBuffer(json.user.id),
      },
      excludeCredentials: json.excludeCredentials?.map(c => ({
        type: c.type,
        id: base64urlToArrayBuffer(c.id),
        transports: c.transports as AuthenticatorTransport[] | undefined,
      })),
    };
  }


  export function cleanPublicKeyCreationOptions(
    pk: PublicKeyCredentialCreationOptions
  ): PublicKeyCredentialCreationOptions {
    const cleaned: any = {
      rp: pk.rp,
      user: pk.user,
      challenge: pk.challenge,
      pubKeyCredParams: pk.pubKeyCredParams,
      authenticatorSelection: pk.authenticatorSelection,
      excludeCredentials: pk.excludeCredentials ?? [],
      attestation: pk.attestation,
    };
  
    // timeout：只在合法时传
    if (typeof pk.timeout === "number") {
      cleaned.timeout = pk.timeout;
    }
  
    // hints：默认不传（兼容性太差）
    // cleaned.hints = pk.hints ❌
  
    // extensions：只保留“真的有值”的
    if (pk.extensions) {
      const exts = Object.fromEntries(
        Object.entries(pk.extensions).filter(
          ([_, v]) => v !== null && v !== undefined
        )
      );
  
      if (Object.keys(exts).length > 0) {
        cleaned.extensions = exts;
      }
    }
  
    return cleaned;
  }


  export function toPublicKeyCredentialRequestOptions(
    json: PublicKeyCredentialRequestOptionsJSON
  ): PublicKeyCredentialRequestOptions {
    return {
      challenge: base64urlToArrayBuffer(json.challenge),
      rpId: json.rpId ?? undefined,
      timeout: json.timeout ?? undefined,
      userVerification: (json.userVerification as UserVerificationRequirement) ?? undefined,
      allowCredentials: (json.allowCredentials ?? []).map((c) => ({
        type: c.type,
        id: base64urlToArrayBuffer(c.id),
        transports: c.transports as AuthenticatorTransport[] | undefined,
      })),
      extensions: json.extensions as any,
    };
  }
  
  export function cleanPublicKeyRequestOptions(
    pk: PublicKeyCredentialRequestOptions
  ): PublicKeyCredentialRequestOptions {
    const cleaned: any = {
      challenge: pk.challenge,
      rpId: pk.rpId,
      allowCredentials: pk.allowCredentials ?? [],
      userVerification: pk.userVerification,
    };
  
    if (typeof pk.timeout === "number") {
      cleaned.timeout = pk.timeout;
    }
  
    // extensions：只保留非 null/undefined
    if ((pk as any).extensions) {
      const exts = Object.fromEntries(
        Object.entries((pk as any).extensions).filter(([_, v]) => v !== null && v !== undefined)
      );
      if (Object.keys(exts).length > 0) cleaned.extensions = exts;
    }
  
    return cleaned;
  }


  // src/features/passkey/utils/webauthn.ts

export function base64urlToArrayBuffer(base64url: string): ArrayBuffer {
    const padding = "=".repeat((4 - (base64url.length % 4)) % 4);
    const base64 = (base64url + padding)
      .replace(/-/g, "+")
      .replace(/_/g, "/");
  
    const raw = atob(base64);
    const bytes = new Uint8Array(raw.length);
    for (let i = 0; i < raw.length; i++) {
      bytes[i] = raw.charCodeAt(i);
    }
    return bytes.buffer;
  }
  
  export function arrayBufferToBase64url(buf: ArrayBuffer): string {
    const bytes = new Uint8Array(buf);
    let str = "";
    for (const b of bytes) str += String.fromCharCode(b);
    return btoa(str)
      .replace(/\+/g, "-")
      .replace(/\//g, "_")
      .replace(/=+$/g, "");
  }

  export function credentialToJson(cred: PublicKeyCredential) {
    const anyCred: any = cred;
  
    const json: any = {
      id: cred.id,
      type: cred.type,
      // ✅ rawId 必须从 ArrayBuffer 转 base64url
      rawId: arrayBufferToBase64url(anyCred.rawId as ArrayBuffer),
      response: {},
    };
  
    const resp: any = cred.response;
  
    // ✅ 注册：AuthenticatorAttestationResponse
    if (resp.attestationObject) {
      json.response.clientDataJSON = arrayBufferToBase64url(resp.clientDataJSON);
      json.response.attestationObject = arrayBufferToBase64url(resp.attestationObject);
    }
    // ✅ 登录：AuthenticatorAssertionResponse
    else {
      json.response.clientDataJSON = arrayBufferToBase64url(resp.clientDataJSON);
      json.response.authenticatorData = arrayBufferToBase64url(resp.authenticatorData);
      json.response.signature = arrayBufferToBase64url(resp.signature);
      json.response.userHandle = resp.userHandle ? arrayBufferToBase64url(resp.userHandle) : null;
    }
  
    return json;
  }
  