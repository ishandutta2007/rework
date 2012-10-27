import sys, csv, os.path, datetime, string, sqlite3

# TODO try pandas next time and see if its less coding than this

dataDir = os.path.expanduser('~/rework/competitions/loadForecasting/data/')

conn = sqlite3.connect(':memory:')
curs = conn.cursor()

# this source file from Kaggle is checked into git
with open(dataDir + 'temperature_history.csv', 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)
    to_db = [(i['station_id'], i['year'], i['month'], i['day'], i['h1'], i['h2'], i['h3'], i['h4'], i['h5'], i['h6'], i['h7'], i['h8'], i['h9'], i['h10'], i['h11'], i['h12'], i['h13'], i['h14'], i['h15'], i['h16'], i['h17'], i['h18'], i['h19'], i['h20'], i['h21'], i['h22'], i['h23'], i['h24']) for i in reader]

curs.execute('CREATE TABLE t (station_id INT, year INT, month INT, day INT, h1 INT, h2 INT, h3 INT, h4 INT, h5 INT, h6 INT, h7 INT, h8 INT, h9 INT, h10 INT, h11 INT, h12 INT, h13 INT, h14 INT, h15 INT, h16 INT, h17 INT, h18 INT, h19 INT, h20 INT, h21 INT, h22 INT, h23 INT, h24 INT);')
curs.executemany('INSERT INTO t (station_id, year, month, day, h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15, h16, h17, h18, h19, h20, h21, h22, h23, h24) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);', to_db)
conn.commit()

curs.execute('CREATE INDEX IF NOT EXISTS index_t_year ON t (year)')
curs.execute('CREATE INDEX IF NOT EXISTS index_t_month ON t (month)')
curs.execute('CREATE INDEX IF NOT EXISTS index_t_day ON t (day)')
curs.execute('CREATE INDEX IF NOT EXISTS index_t_station_id ON t (station_id)')

# this source file from Kaggle is checked into git
with open(dataDir + 'Load_history.csv', 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)
    
    # TODO upgrade to argparse if I ever reuse this script
    with open(dataDir + sys.argv[1],'wb') as csvoutfile:
        writer = csv.DictWriter(csvoutfile, fieldnames=['load',
                                                        'zone_id',
                                                        'day_of_year',
                                                        'hour_of_day',
                                                        'year',
                                                        't1',
                                                        't2',
                                                        't3',
                                                        't4',
                                                        't5',
                                                        't6',
                                                        't7',
                                                        't8',
                                                        't9',
                                                        't10',
                                                        't11',
                                                        ])
        writer.writeheader()
        for row in reader:
            for hour in range(1,25):
                hourColumnName = 'h'+str(hour)
                load = row[hourColumnName].replace(',','')
            
                if(0 == len(load)):
                    print('Skipping empty load value')
                    continue
                
                timepoint = datetime.datetime(int(row['year']),
                                              int(row['month']),
                                              int(row['day']),
                                              hour-1,
                                              30)
                newRow = {
                    'load': load,
                    'zone_id': row['zone_id'],
                    'day_of_year': timepoint.strftime ('%j'),
                    'hour_of_day': timepoint.strftime ('%H'),
                    'year': timepoint.strftime ('%Y')
                    }

                # Add the temperatures measured at each station, as applicable
                for station in range(1,12):
                    # try:
                    curs.execute('SELECT ' + hourColumnName + ' from t WHERE '
                                 + row['year'] + ' = t.year AND '
                                 + row['month'] + ' = t.month AND '
                                 + row['day'] + ' = t.day AND '
                                 + str(station) + ' = t.station_id')
                    temp = curs.fetchone()[0]
                    if(temp == u''):
                        # redundant since we'll wind up with an NA anyway
                        continue
                    newRow['t'+str(station)] = temp
                    # TODO cache this reformatted temp data since we need it
                    # 19 more times for the other zones
                
                writer.writerow(newRow)
