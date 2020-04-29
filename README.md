# Daily Unix

Get a notification every day with a new unix command.

## Android app

The app has the content zipped up into its `res/raw` directory. See `android`.

## Scripts to generate the content zip from a unix host

### Node version (unmaintained)

Use node v8.11.3 or later.

`npm install`
`npm run build`
`npm run run`

### Python version (current)

```
virtualenv -p /usr/bin/python3.7 venv
source venv/bin/activate
python ./index.py
deactivate
```

Then take a look at `./out` and `./py/content.zip`

The problem with this script alone is that it requires a development machine to run, ie. one with
Python 3, git, etc. It's nice to be able to run this on a machine with only a minimal Ubuntu
installation, so that the commands indexed are only those relevant to non-developer users. Enter
PyOxidizer, a tool for creating static binaries from Python scripts.

See: https://pyoxidizer.readthedocs.io/en/stable/getting_started.html
