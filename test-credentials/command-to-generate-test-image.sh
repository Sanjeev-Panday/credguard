magick -size 1600x2000 xc:white \
  -gravity northwest \
  -pointsize 38 \
  -font Helvetica \
  -fill black \
  -annotate +100+100 "Verifiable Credential

ID: cred-12345
Issuer: Example University
Subject: did:example:student123
Degree: Bachelor of Science
Major: Computer Science
Issued: 2024-01-15
Expires: 2025-01-15" \
  output.png
