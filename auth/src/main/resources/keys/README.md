# Development keys

`dev-private.pem` (PKCS#8) signs tokens, `dev-public.pem` (X.509) verifies them.
The public key is copied into every service that verifies a token; the private
key exists only here, in `auth`, because only `auth` mints tokens.

These are committed so the stack runs out of the box. **They are not secret and
must not be used anywhere real.** Point the services at your own pair with:

    TTARENA_JWT_PRIVATE_KEY=file:/run/secrets/jwt-private.pem   # auth only
    TTARENA_JWT_PUBLIC_KEY=file:/run/secrets/jwt-public.pem     # every service

Generate a fresh pair with:

    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
    openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
