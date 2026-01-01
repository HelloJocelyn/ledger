/**
 * JSON-serializable PublicKeyCredentialCreationOptions
 * (ArrayBuffer fields are base64url-encoded strings)
 */
export interface PublicKeyCredentialCreationOptionsJSON {
    rp: {
      id?: string;
      name: string;
    };
  
    user: {
      id: string;          // base64url(userHandle bytes)
      name: string;        // username (email / phone)
      displayName: string;
    };
  
    challenge: string;     // base64url
  
    pubKeyCredParams: Array<{
      type: "public-key";
      alg: number;
    }>;
  
    timeout?: number;
  
    excludeCredentials?: Array<{
      type: "public-key";
      id: string;          // base64url credentialId
      transports?: AuthenticatorTransport[];
    }>;
  
    authenticatorSelection?: {
      authenticatorAttachment?: "platform" | "cross-platform";
      residentKey?: "required" | "preferred" | "discouraged";
      userVerification?: "required" | "preferred" | "discouraged";
    };
  
    attestation?: "none" | "indirect" | "direct" | "enterprise";
  
    extensions?: Record<string, unknown>;
  }


  export type AuthenticatorTransport =
  | "usb"
  | "nfc"
  | "ble"
  | "internal"
  | "hybrid";

  