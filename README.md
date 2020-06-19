# Daily Unix

Get a notification every day with a new unix command.

## Android app

The app has the content zipped up into its `res/raw` directory. See `android`.

## Scripts to generate the content zip from a unix host

### Python version (current)

```
cd py
virtualenv -p /usr/bin/python3.7 venv
source venv/bin/activate
python ./index.py
deactivate
```

Then take a look at `./out` and `./py/content.zip`. If it's good, overwite the copy in the Android app:

`cp ./py/content.zip ./android/app/src/main/res/raw/content.zip`

The problem with this script alone is that it requires a development machine to run, ie. one with
Python 3, git, etc. It's nice to be able to run this on a machine with only a minimal Ubuntu
installation, so that the commands indexed are only those relevant to non-developer users. Enter
PyOxidizer, a tool for creating static binaries from Python scripts. To run this on a minimal
host, first do this on the development host:

1. Follow the PyOxidizer [Getting Started](https://pyoxidizer.readthedocs.io/en/stable/getting_started.html#)
1. `cd py`
1. `pyoxidizer build`
1. `cp ./build/x86_64-unknown-linux-gnu/debug/exe/pyoxidizer ../daily-unix-index`
1. `cd ..`
1. `git add ./daily-unix-index && git commit -m "New indexer PyOxidizer build." && git push origin master`

Then on the minimal host:

1. `sudo apt-get install groff` (we do need groff installed in order to generate HTML output of the man pages)
1. Grab the `daily-unix-index` binary from https://github.com/eliotstock/daily-unix using a browser
1. Grab the TLDR pages repo zip from https://github.com/tldr-pages/tldr using a browser and unzip them such that the `tldr` directory sits alongside the `daily-unix-index` binary
1. `chmod a+x daily-unix-index`
1. `.daily-unix-index/`
1. Use drive.google.com from the browser to move the content.zip file over to the development host for committing.

### Node version (unmaintained)

Use node v8.11.3 or later.

```
npm install
npm run build
npm run run
```
