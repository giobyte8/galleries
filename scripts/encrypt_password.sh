#!/usr/bin/env bash
# Usage: ./encrypt_password.sh <plaintext-password>
python -c "import bcrypt, sys; print(bcrypt.hashpw(sys.argv[1].encode(), bcrypt.gensalt()).decode())" "$1"

