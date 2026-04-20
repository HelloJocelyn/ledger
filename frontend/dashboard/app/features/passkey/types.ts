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

  /**
 * JSON-serializable credential descriptor
 */
export interface PublicKeyCredentialDescriptorJSON {
  /**
   * Credential type, always "public-key"
   */
  type: PublicKeyCredentialType; // "public-key"

  /**
   * Base64URL-encoded credential ID
   */
  id: string;

  /**
   * Supported authenticator transports (optional)
   */
  transports?: AuthenticatorTransport[];
}



  /**
 * JSON-serializable version of PublicKeyCredentialRequestOptions
 * Used for Passkey authentication (navigator.credentials.get)
 */
export interface PublicKeyCredentialRequestOptionsJSON {
  /**
   * Base64URL-encoded challenge (required)
   */
  challenge: string;

  /**
   * Relying Party ID (optional)
   * Usually your domain, e.g. "example.com"
   */
  rpId?: string;

  /**
   * Timeout in milliseconds (optional)
   */
  timeout?: number;

  /**
   * Allowed credentials for this authentication ceremony
   * If omitted or empty, discoverable credentials may be used
   */
  allowCredentials?: PublicKeyCredentialDescriptorJSON[];

  /**
   * User verification requirement
   * "required" | "preferred" | "discouraged"
   */
  userVerification?: UserVerificationRequirement;

  /**
   * WebAuthn extensions (optional)
   * Only include keys you actually use
   */
  extensions?: AuthenticationExtensionsClientInputs;
}



  export type AuthenticatorTransport =
  | "usb"
  | "nfc"
  | "ble"
  | "internal"
  | "hybrid";

  