# Daily Unix

Script to generate static content for the Daily Unix Android app.

## Building (node only)

Use node v8.11.3 or later.

`npm install`
`npm run build`

## Running

### Node

`npm run run`

### Python

```
virtualenv -p /usr/bin/python3.7 venv
source venv/bin/activate
python ./index.py
deactivate
```

Then take a look at `./out`.
