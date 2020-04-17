# Copyright 2020 Eliot Stock
"""Unowned files. Find files on this host that are not owned by any package."""

import logging
import os
import shutil
import subprocess
import sys

_LOG = logging.getLogger(__name__)
logging.basicConfig(stream=sys.stdout, level=logging.DEBUG)

# Directories that contain binaries
_BIN_DIRS = ['/bin', '/sbin', '/usr/bin', '/usr/sbin']

_OUT_DIR = '../out'

def main() -> int:
    """Script entry point."""

    _LOG.info(f'Checking for required tools')

    if not os.path.exists('../../tldr'):
        _LOG.error('Clone https://github.com/tldr-pages/tldr.git into directory tldr alongside this repo')
        exit

    _LOG.info('Removing output from last time')
    try:
        shutil.rmtree(_OUT_DIR)
    except Exception:
        pass

    os.makedirs(_OUT_DIR + '/ls')
    os.makedirs(_OUT_DIR + '/man')

    total_man_pages = 0
    total_tldr_pages = 0

    for d in _BIN_DIRS:
        os.makedirs(f'{_OUT_DIR}/ls{d}')
        os.makedirs(f'{_OUT_DIR}/man{d}')
        os.makedirs(f'{_OUT_DIR}/tldr{d}')

        ls_out = open(f'{_OUT_DIR}/ls{d}/ls.txt', 'w')
        subprocess.call(['ls', '-1', d], stdout=ls_out)

        _LOG.info(f'Saving pages for {d}')

        bin_list_lines = open(f'{_OUT_DIR}/ls{d}/ls.txt').readlines()

        dir_man_pages = 0
        dir_tldr_pages = 0

        for b in bin_list_lines:
            if len(b) == 0:
                pass

            b = b.strip()

            # Produce man pages
            man_out = open(f'{_OUT_DIR}/man{d}/{b}.txt', 'w')
            # TODO: Take stderr here and write it to _LOG.debug().
            subprocess.call(['man', b], stdout=man_out)
            dir_man_pages += 1
            total_man_pages += 1

            # Try looking for the tldr page first under common, and only if
            # that fails under linux.
            try:
                shutil.copy(f'../../tldr/pages/common/{b}.md', f'{_OUT_DIR}/tldr/{d}/{b}.md')
                dir_tldr_pages += 1
                total_tldr_pages += 1
            except Exception:
                try:
                    shutil.copy(f'../../tldr/pages/linux/{b}.md', f'{_OUT_DIR}/tldr/{d}/{b}.md')
                    dir_tldr_pages += 1
                    total_tldr_pages += 1
                except Exception:
                    _LOG.debug(f'  No tldr page: {d}/{b}')
        _LOG.info(f'  {dir_man_pages} man pages')
        _LOG.info(f'  {dir_tldr_pages} tldr pages')

    _LOG.info(f'Total: {total_man_pages} man pages')
    _LOG.info(f'Total: {total_tldr_pages} tldr pages')

if __name__ == '__main__':
    sys.exit(main())
