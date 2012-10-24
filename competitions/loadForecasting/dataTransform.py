import csv, os.path, datetime, string

dataDir = os.path.expanduser('~/rework/competitions/loadForecasting/data/')

with open(dataDir + 'Load_history.csv', 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)
    
    with open(dataDir + 'Load_history_timeseries.csv','wb') as csvoutfile:
        writer = csv.DictWriter(csvoutfile, fieldnames=['zone_id',
                                                        'timepoint',
                                                        'load'])
        writer.writeheader()
        for row in reader:
            for hour in range(1,25):
                timepoint = datetime.datetime(int(row['year']),
                                              int(row['month']),
                                              int(row['day']),
                                              hour-1,
                                              30)
                writer.writerow({'zone_id': row['zone_id'],
                                 'timepoint': timepoint.isoformat(),
                                 'load': row['h'+str(hour)].replace(',','')})
