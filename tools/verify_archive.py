#!/usr/bin/env python3
"""Verify restored baseline files without comparing generated build/cache files."""
import hashlib
import json
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parents[1]
manifest = json.loads((root / 'docs/reconstruction/archive-manifest.json').read_text())
errors = []
archive = root / 'test.zip'
if hashlib.sha256(archive.read_bytes()).hexdigest() != manifest['archive_sha256']:
    errors.append('Archive checksum changed')
with zipfile.ZipFile(archive) as reference:
    for entry in manifest['files']:
        path = root / entry['path']
        if not path.is_file() or path.read_bytes() != reference.read(entry['path']):
            errors.append(entry['path'])
print('\n'.join(errors) if errors else f"PASS: {len(manifest['files'])} baseline files match byte for byte.")
raise SystemExit(bool(errors))
