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
from bs4 import BeautifulSoup

_LOG = logging.getLogger(__name__)
logging.basicConfig(stream=sys.stdout, level=logging.INFO)

# Directories that contain binaries. On Ubuntu 20.04, /bin and /sbin are just
# symlinks to the same dirs under /usr. To check manually whether this host
# has a bunch of stuff in both dirs:
#   comm -12 <(ls -1 /usr/bin) <(ls -1 /usr/sbin)
_BIN_DIRS = ['/usr/bin', '/usr/sbin']

_TLDR_DIR = './tldr'

_OUT_DIR = '../out'

class CsvCoverageRow:
    """Struct-like data for a row in the coverage.csv that we produce."""
    bin: str
    whatIs: str
    tldr: bool
    package: str
    man: bool

def main() -> int:
    """Script entry point."""

    _LOG.info(f'Checking for required tools')

    if not os.path.exists(_TLDR_DIR):
        _LOG.error('Please run:\ngit clone https://github.com/tldr-pages/tldr.git'
                + f' {_TLDR_DIR}')
        return 1

    # input('Hit Ctrl-C now to stop re-run.')

    _LOG.info('Removing output from last time')
    try:
        shutil.rmtree(_OUT_DIR)
    except Exception:
        pass

    os.makedirs(_OUT_DIR)

    coverage_csv_out = open(f'{_OUT_DIR}/coverage.csv', 'w')
    coverage_csv_out.write('command,whatis,tldr,package,man\n')
    coverage_csv_row = CsvCoverageRow()

    total_man_pages = 0
    total_tldr_pages = 0

    for d in _BIN_DIRS:
        bins = subprocess.check_output(['ls', '-1', d]).splitlines()

        _LOG.info(f'Saving pages for {d}')

        dir_man_pages = 0
        dir_tldr_pages = 0

        for b in bins:
            if len(b) == 0:
                continue

            b = b.strip().decode()

            try:
                os.makedirs(f'{_OUT_DIR}/{b}')
            except FileExistsError:
                # If a binary exists in both /bin and /sbin, we already have
                # what we need and can skip it this time.
                continue

            coverage_csv_row = CsvCoverageRow()
            coverage_csv_row.bin = b

            # Try looking for the tldr page first under common, and only if
            # that fails under linux.
            coverage_csv_row.tldr = False

            try:
                shutil.copy(f'{_TLDR_DIR}/pages/common/{b}.md',
                        f'{_OUT_DIR}/{b}/tldr.md')
                dir_tldr_pages += 1
                total_tldr_pages += 1
                coverage_csv_row.tldr = True
            except Exception:
                try:
                    shutil.copy(f'{_TLDR_DIR}/pages/linux/{b}.md',
                            f'{_OUT_DIR}/{b}/tldr.md')
                    dir_tldr_pages += 1
                    total_tldr_pages += 1
                    coverage_csv_row.tldr = True
                except Exception:
                    _LOG.debug(f'  No tldr page: {d}/{b}')

                    # For now, to reduce scope for the user, only cover
                    # commands that have a TLDR. While this is truly *daily*
                    # unix commands, having 1,800 commands to get through
                    # will take the user 5 years.
                    shutil.rmtree(f'{_OUT_DIR}/{b}')
                    continue

            # Produce whatis strings.
            coverage_csv_row.whatIs = ''

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
                    coverage_csv_row.whatIs = whatis
            except Exception:
                pass

            # Note the owning package of the binary.
            coverage_csv_row.package = ''

            try:
                package_process = subprocess.Popen(['dpkg', '-S', f'{d}/{b}'],
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE)
                package_stdout = package_process.stdout.read()
                package_stderr = package_process.stderr.read()

                # Take just the first word of the first line, before ":".
                package = package_stdout.strip().decode().split(':')[0]

                if package_stderr:
                    if 'no path found matching pattern' in package_stderr.decode():
                        # Let's use the empty string as a valid value for
                        # package, meaning this binary isn't installed as part
                        # of a package on this Linux distro.
                        package = ''
                    else:
                        _LOG.warning(package_stderr.decode().strip())
                        shutil.rmtree(f'{_OUT_DIR}/{b}')
                        continue

                package_out = open(f'{_OUT_DIR}/{b}/package.txt', 'w')
                if package:
                    package_out.write(package)
                    package_out.close()
                    coverage_csv_row.package = package
            except Exception:
                shutil.rmtree(f'{_OUT_DIR}/{b}')
                continue

            # Produce man pages
            coverage_csv_row.man = False

            # As plain text, without justifying the text but with a default width. This
            # introduces line breaks where we don't want them.
            # man_out = open(f'{_OUT_DIR}/{b}/man.txt', 'w')
            # man_process = subprocess.Popen(['man', '--no-justification', b], stdout=man_out,
            #         stderr=subprocess.PIPE)
            # man_error = man_process.stderr.read()
            # if man_error:
            #     _LOG.warning(man_error.decode().strip())
            # else:
            #     coverage_csv_row.man = True

            # As Postscript files, which look quite nice in a PS reader, but contain page breaks.
            # man_out = open(f'{_OUT_DIR}/{b}/man.ps', 'w')
            # man_process = subprocess.Popen(['man', '-t', b], stdout=man_out,
            #         stderr=subprocess.PIPE)
            # man_error = man_process.stderr.read()
            # if man_error:
            #     _LOG.warning(man_error.decode().strip())

            # As HTML, which has no hard line breaks and is all in one page, but has the downside
            # that there is stuff we don't want at the top like this:
            # <style type="text/css">
            # p       { margin-top: 0; margin-bottom: 0; vertical-align: top }
            # pre     { margin-top: 0; margin-bottom: 0; vertical-align: top }
            # table   { margin-top: 0; margin-bottom: 0; vertical-align: top }
            # h1      { text-align: center }
            # </style>
            # ...
            # <body>
            # <h1 align="center">cupsfilter</h1>
            # <a href="#NAME">NAME</a><br>
            # <a href="#SYNOPSIS">SYNOPSIS</a><br>
            # <a href="#DESCRIPTION">DESCRIPTION</a><br>
            # <a href="#OPTIONS">OPTIONS</a><br>
            # <a href="#EXIT STATUS">EXIT STATUS</a><br>
            # <a href="#ENVIRONMENT">ENVIRONMENT</a><br>
            # <a href="#FILES">FILES</a><br>
            # <a href="#NOTES">NOTES</a><br>
            # <a href="#EXAMPLE">EXAMPLE</a><br>
            # <a href="#SEE ALSO">SEE ALSO</a><br>
            # <a href="#COPYRIGHT">COPYRIGHT</a><br>
            # <hr>
            # <h2>NAME
            # <a name="NAME"></a>
            # </h2>
            # TODO (P1): Find out what section the command is in. Not everything of interest is in
            # section 1.
            man_out = open(f'{_OUT_DIR}/{b}/man.html', 'w')
            gunzip = subprocess.Popen(['gunzip', '--to-stdout', f'/usr/share/man/man1/{b}.1.gz'],
                    stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            groff = subprocess.Popen(['groff', '-mandoc', '-Thtml'],
                    stdin=gunzip.stdout, stdout=man_out, stderr=subprocess.PIPE)
            groff.wait()
            gunzip_error = gunzip.stderr.read()
            groff_error = groff.stderr.read()
            if gunzip_error:
                _LOG.warning(f'/usr/share/man/man1/{b}.1.gz:')
                _LOG.warning(gunzip_error.decode().strip())
            elif groff_error:
                _LOG.warning(f'/usr/share/man/man1/{b}.1.gz:')
                _LOG.warning(groff_error.decode().strip())
            else:
                coverage_csv_row.man = True

            # Remove everything in the following tags: <style>, <h1>, <hr>, <a>.
            with open(f'{_OUT_DIR}/{b}/man.html', 'r') as html_file:
                html = html_file.read()
                soup = BeautifulSoup(html, 'html.parser')
                try:
                    soup.style.extract()
                    soup.h1.extract()
                    soup.hr.extract()

                    links = soup.find_all('a')
                    for link in links:
                        link.extract()
                except Exception:
                    # This is likely to be just that the HTML generated doesn't have any A tags in
                    # it. No problem.
                    pass

                # _LOG.info(f'Cleaned HTML: {str(soup)}')

            with open(f'{_OUT_DIR}/{b}/man.html', 'w') as html_file:
                html_file.write(str(soup))
                html_file.close()

            dir_man_pages += 1
            total_man_pages += 1

            # Write out CSV row to the file.
            coverage_csv_out.write(f'{coverage_csv_row.bin},{coverage_csv_row.whatIs},'
                    + f'{coverage_csv_row.tldr},{coverage_csv_row.package},'
                    + f'{coverage_csv_row.man}\n')

            _LOG.info(f'  {d}/{b} ({coverage_csv_row.package}): {coverage_csv_row.whatIs}')

        # Done with this binary directory.
        _LOG.info(f'  {dir_man_pages} man pages')
        _LOG.info(f'  {dir_tldr_pages} tldr pages')

    # Done with all binary directories.
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

    return 0

if __name__ == '__main__':
    sys.exit(main())
