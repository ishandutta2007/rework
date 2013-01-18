import csv, sqlite3, sys, os.path, datetime, string

dataDir = os.path.expanduser('~/rework/competitions/loadForecasting/data/')

conn = sqlite3.connect(":memory:")
curs = conn.cursor()

# this source file from Kaggle is checked into git
with open(dataDir + 'temperature_history.csv', 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)
    to_db = [(i['station_id'], i['year'], i['month'], i['day'], i['h1'], i['h2'], i['h3'], i['h4'], i['h5'], i['h6'], i['h7'], i['h8'], i['h9'], i['h10'], i['h11'], i['h12'], i['h13'], i['h14'], i['h15'], i['h16'], i['h17'], i['h18'], i['h19'], i['h20'], i['h21'], i['h22'], i['h23'], i['h24']) for i in reader]

curs.execute("CREATE TABLE t (station_id INT, year INT, month INT, day INT, h1 INT, h2 INT, h3 INT, h4 INT, h5 INT, h6 INT, h7 INT, h8 INT, h9 INT, h10 INT, h11 INT, h12 INT, h13 INT, h14 INT, h15 INT, h16 INT, h17 INT, h18 INT, h19 INT, h20 INT, h21 INT, h22 INT, h23 INT, h24 INT);")
curs.executemany("INSERT INTO t (station_id, year, month, day, h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15, h16, h17, h18, h19, h20, h21, h22, h23, h24) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", to_db)
conn.commit()


# this source file from Kaggle is checked into git
with open(dataDir + 'Load_history.csv', 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)
    to_db = [(i['zone_id'], i['year'], i['month'], i['day'], i['h1'].replace(',',''), i['h2'].replace(',',''), i['h3'].replace(',',''), i['h4'].replace(',',''), i['h5'].replace(',',''), i['h6'].replace(',',''), i['h7'].replace(',',''), i['h8'].replace(',',''), i['h9'].replace(',',''), i['h10'].replace(',',''), i['h11'].replace(',',''), i['h12'].replace(',',''), i['h13'].replace(',',''), i['h14'].replace(',',''), i['h15'].replace(',',''), i['h16'].replace(',',''), i['h17'].replace(',',''), i['h18'].replace(',',''), i['h19'].replace(',',''), i['h20'].replace(',',''), i['h21'].replace(',',''), i['h22'].replace(',',''), i['h23'].replace(',',''), i['h24'].replace(',','')) for i in reader]

curs.execute("CREATE TABLE l (zone_id INT, year INT, month INT, day INT, h1 INT, h2 INT, h3 INT, h4 INT, h5 INT, h6 INT, h7 INT, h8 INT, h9 INT, h10 INT, h11 INT, h12 INT, h13 INT, h14 INT, h15 INT, h16 INT, h17 INT, h18 INT, h19 INT, h20 INT, h21 INT, h22 INT, h23 INT, h24 INT, t1 INT, t2 INT, t3 INT, t4 INT, t5 INT, t6 INT, t7 INT, t8 INT, t9 INT, t10 INT, t11 INT);")
curs.executemany("INSERT INTO l (zone_id, year, month, day, h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15, h16, h17, h18, h19, h20, h21, h22, h23, h24) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", to_db)
conn.commit()

curs.execute("CREATE INDEX IF NOT EXISTS index_t_year ON t (year)")
curs.execute("CREATE INDEX IF NOT EXISTS index_t_month ON t (month)")
curs.execute("CREATE INDEX IF NOT EXISTS index_t_day ON t (day)")
curs.execute("CREATE INDEX IF NOT EXISTS index_l_year ON l (year)")
curs.execute("CREATE INDEX IF NOT EXISTS index_l_month ON l (month)")
curs.execute("CREATE INDEX IF NOT EXISTS index_l_day ON l (day)")


curs.execute("SELECT count(*) FROM l JOIN t ON l.year = t.year AND l.month = t.month AND l.day = t.day")
print(curs.fetchone())

curs.execute("UPDATE l INNER JOIN t ON l.year = t.year AND l.month = t.month AND l.day = t.day AND t.station_id=1 SET l.t1 = t.h1")
