import type { PublicKeyCredentialCreationOptionsJSON, AuthenticatorTransport } from '../types';

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
    const response = cred.response as AuthenticatorAttestationResponse;
  
    return {
      id: cred.id,
      rawId: arrayBufferToBase64url(cred.rawId),
      type: cred.type,
      response: {
        clientDataJSON: arrayBufferToBase64url(response.clientDataJSON),
        attestationObject: arrayBufferToBase64url(response.attestationObject),
        transports: response.getTransports
          ? response.getTransports()
          : undefined,
      },
    };
  }
  