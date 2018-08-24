'use strict';

import * as cp from 'child_process';
import * as fs from 'fs';

const binDirs: Array<string> = ['/bin', '/sbin', '/usr/bin', '/usr/sbin'];

console.log('Checking for required tools');

let out = cp.execSync('test -d ../../tldr; echo $?;').toString();
if (out && out.includes('1')) {
    console.error('Clone https://github.com/tldr-pages/tldr.git into directory tldr alongside this repo');
    process.exit();
}

console.log('Removing output from last time');
cp.execSync('rm -rf ./out || true');
cp.execSync('mkdir ./out');
cp.execSync('mkdir ./out/ls');
cp.execSync('mkdir ./out/man');

let totalManPages = 0;
let totalTldrPages = 0;

for (let binDir of binDirs) {
    cp.execSync(`mkdir -p ./out/ls/${binDir}`);
    cp.execSync(`mkdir -p ./out/man/${binDir}/`);
    cp.execSync(`mkdir -p ./out/tldr/${binDir}/`);
    cp.execSync(`ls -1 ${binDir} &> ./out/ls/${binDir}/ls.txt`);

    console.log(`Saving pages for ${binDir}`);

    const binListLines = fs.readFileSync(`./out/ls/${binDir}/ls.txt`, 'utf-8').split('\n');

    let dirManPages = 0;
    let dirTldrPages = 0;

    for (let bin of binListLines) {
        if (bin.length > 0) {
            // Produce man pages.
            try {
                cp.execSync(`man ${bin} &> ./out/man/${binDir}/${bin}.txt`);
                dirManPages++;
                totalManPages++;
            }
            catch (err) {
                // console.error(`  No man page: ${binDir}/${bin}`);
            }

            // Try looking for the tldr page first under common, and only if that fails under linux. Don't touch osx for now.
            try {
                cp.execSync(`cp ../../tldr/pages/common/${bin}.md ./out/tldr/${binDir}/${bin}.md &> /dev/null`);
                dirTldrPages++;
                totalTldrPages++;
            }
            catch (err) {
                try {
                    cp.execSync(`cp ../../tldr/pages/linux/${bin}.md ./out/tldr/${binDir}/${bin}.md &> /dev/null`);
                    dirTldrPages++;
                    totalTldrPages++;
                }
                catch (err) {
                    // console.error(`  No tldr page: ${binDir}/${bin}`);
                }
            }
        }
    }

    console.log(`  ${dirManPages} man pages`);
    console.log(`  ${dirTldrPages} tldr pages`);
}

console.log(`Total: ${totalManPages} man pages`);
console.log(`Total: ${totalTldrPages} tldr pages`);
