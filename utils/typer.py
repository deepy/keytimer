from __future__ import with_statement
from com.ziclix.python.sql import zxJDBC

import time

import sys

jdbc_url = "jdbc:h2:~/timings"
username = "sa"
password = ""
driver = "org.h2.Driver"


keys = {}

# obtain a connection using the with-statment
with zxJDBC.connect(jdbc_url, username, password, driver) as conn:
    with conn:
        with conn.cursor() as c:
            c.execute("SELECT key1, key2, avg(diff) FROM TIMINGS GROUP BY key1, key2 order by key1, key2")
            for pair in c.fetchall():
                if pair[0] == '3':
                    continue
                else:
                    try:
                        keys[pair[0]][pair[1]] = pair[2]
                    except:
                        keys[pair[0]] = {pair[1]:pair[2]}

print "What to write?"
sentence = raw_input('> ')


previous = None
for letter in sentence:
    if previous is not None:
        try:
            time.sleep(keys[previous.upper()][letter.upper()]/1000.0)
        except:
            time.sleep(84/1000.0)
    sys.stdout.write(letter)
    sys.stdout.flush()
    previous = letter