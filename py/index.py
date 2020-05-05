# Copyright 2020 Eliot Stock
"""Create a static set of man pages, tldr pages and whatis strings for all the
   binaries ("commands") on this host."""

import logging
import os
import re
import shutil
import subprocess
import sys
import zipfile

from os.path import basename

_LOG = logging.getLogger(__name__)
logging.basicConfig(stream=sys.stdout, level=logging.INFO)

# Directories that contain binaries. On Ubuntu 20.04, /bin and /sbin are just
# symlinks to the same dirs under /usr.
_BIN_DIRS = ['/usr/bin', '/usr/sbin']

_OUT_DIR = '../out'

def main() -> int:
    """Script entry point."""

    _LOG.info(f'Checking for required tools')

    if not os.path.exists('../../tldr'):
        _LOG.error('Clone https://github.com/tldr-pages/tldr.git into'
                + ' directory tldr alongside this repo')
        exit

    # input('Hit Ctrl-C now to stop re-run.')

    _LOG.info('Removing output from last time')
    try:
        shutil.rmtree(_OUT_DIR)
    except Exception:
        pass

    os.makedirs(_OUT_DIR)

    coverage_csv_out = open(f'{_OUT_DIR}/coverage.csv', 'w')
    coverage_csv_out.write('command,whatis,package,man,tldr\n')
    coverage_csv_row = ''

    total_man_pages = 0
    total_tldr_pages = 0

    for d in _BIN_DIRS:
        bins = subprocess.check_output(['ls', '-1', d]).splitlines()

        _LOG.info(f'Saving pages for {d}')

        dir_man_pages = 0
        dir_tldr_pages = 0

        for b in bins:
            if len(b) == 0:
                pass

            b = b.strip().decode()

            try:
                os.makedirs(f'{_OUT_DIR}/{b}')
            except FileExistsError:
                # If a binary exists in both /bin and /sbin, we already have
                # what we need and can skip it this time.
                continue

            coverage_csv_row += f'{b},'

            # Produce whatis strings.
            try:
                whatis = subprocess.check_output(['whatis', b],
                        stderr=subprocess.DEVNULL).strip().decode()

                # These strings typically look like this. Let's strip off the
                # left hand side.
                # xmodmap (1)          - utility for modifying keymaps and
                #   pointer button mappings in X
                # Replace "Any characters from start of line up to any amount
                # of whitespace, then a -, then some more whitespace" with
                # nothing.
                whatis = re.sub(r'^.*\s\-\s', '', whatis)

                whatis_out = open(f'{_OUT_DIR}/{b}/whatis.txt', 'w')
                if whatis:
                    whatis_out.write(whatis)
                    whatis_out.close()
                    coverage_csv_row += 'y,'
            except Exception:
                coverage_csv_row += ','

            # Note the owning package of the binary.
            try:
                package = subprocess.check_output(['dpkg', '-S', b],
                    stderr=subprocess.DEVNULL).strip().decode()

                # Take just the first word of the first line, before ":".
                package = package.split(':')[0]

                package_out = open(f'{_OUT_DIR}/{b}/package.txt', 'w')
                if package:
                    package_out.write(package)
                    package_out.close()
                    coverage_csv_row += 'y,'
            except Exception:
                coverage_csv_row += ','

            # Produce man pages
            man_out = open(f'{_OUT_DIR}/{b}/man.txt', 'w')
            man_process = subprocess.Popen(['man', b], stdout=man_out,
                    stderr=subprocess.PIPE)
            error = man_process.stderr.read()
            coverage_csv_row += 'y,'
            if error:
                _LOG.debug(error.decode().strip())

            dir_man_pages += 1
            total_man_pages += 1

            # Try looking for the tldr page first under common, and only if
            # that fails under linux.
            try:
                shutil.copy(f'../../tldr/pages/common/{b}.md',
                        f'{_OUT_DIR}/{b}/tldr.md')
                dir_tldr_pages += 1
                total_tldr_pages += 1
                coverage_csv_row += 'y\n'
            except Exception:
                try:
                    shutil.copy(f'../../tldr/pages/linux/{b}.md',
                            f'{_OUT_DIR}/{b}/tldr.md')
                    dir_tldr_pages += 1
                    total_tldr_pages += 1
                except Exception:
                    _LOG.debug(f'  No tldr page: {d}/{b}')
                coverage_csv_row += '\n'
        _LOG.info(f'  {dir_man_pages} man pages')
        _LOG.info(f'  {dir_tldr_pages} tldr pages')

        coverage_csv_out.write(coverage_csv_row)

    _LOG.info(f'Total: {total_man_pages} man pages')
    _LOG.info(f'Total: {total_tldr_pages} tldr pages')

    coverage_csv_out.close()

    # Zip everything up into a file that can go into the mobile apps
    zip_file = zipfile.ZipFile('content.zip', 'w', zipfile.ZIP_DEFLATED)

    for root, dir, files in os.walk(_OUT_DIR):
        for file in files:
            path_in_fs = os.path.join(root, file)
            path_in_zip = os.path.join(basename(root), file)
            # _LOG.info(f'path_in_fs: {path_in_fs}, path_in_zip: {path_in_zip}')
            zip_file.write(path_in_fs, path_in_zip)
    zip_file.close()

if __name__ == '__main__':
    sys.exit(main())
