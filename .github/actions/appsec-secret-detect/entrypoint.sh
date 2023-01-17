#!/bin/bash
#
# Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# the path to scan for leaked secrets.
REPO_PATH=$1
# if should output a verbose log or not
VERBOSE=$2

# the path to save the scan output
REPORT_PATH="reports/detect-secrets.json"

# setup Yelp/detect-secrets
echo "[INFO] Installing detect-secrets tool"
pip install detect-secrets

# checks wether reports folder exists
if [ ! -d 'reports' ]; then
    echo "[INFO] Creating reports directory"
    mkdir reports
fi

# checks if verbose is used
if [ "$VERBOSE" = "true" ]; then
    echo "[INFO] Running detect-secrets in verbose mode"
    # execute the detect secrets against repo path and output to reports/detect-secrets.json
    detect-secrets -v -C $REPO_PATH scan > $REPORT_PATH
else
    echo "[INFO] Running detect-secrets"
    detect-secrets -C $REPO_PATH scan > $REPORT_PATH
fi

ERROR=$?
# checks if detect-secrets execution returned an error
if [ ! $ERROR -eq 0 ]; then
    echo "[ERROR] Failed to run detect-secrets tool, terminating with code: [ $ERROR ]"
    exit 2
fi

# query the '.results' property of the json
RESULTS="$(cat reports/detect-secrets.json | jq .results)"
# checks if results is empty
if [ "$RESULTS" = "{}" ]; then
    echo "[INFO] Scan finished with NO results"
    exit 0
else
    echo "[INFO] Scan finished, LEAKS were found"
    echo "$RESULTS"
    exit 1
fi
