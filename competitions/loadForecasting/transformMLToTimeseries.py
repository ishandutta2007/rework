import sys, csv, os.path, datetime, string

dataDir = os.path.expanduser('~/rework/competitions/loadForecasting/data/')

with open(dataDir + 'fullLoadAndTempTrainData.csv', 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)

    # TODO upgrade to argparse if I ever reuse this script
    with open(dataDir + sys.argv[1], 'wb') as csvoutfile:
        writer = csv.DictWriter(csvoutfile, fieldnames=['zone_id',
                                                        'timepoint',
                                                        'load',
                                                        'temp'])
        writer.writeheader()
        for row in reader:
            timepoint = datetime.datetime.strptime(row['year']
                                                   + row['day_of_year']
                                                   + ' '
                                                   + row['hour_of_day'],
                                                   "%Y%j %H")
            # See also numpy.mean()
            temp = sum([int(row['t'+str(x)]) for x in range(1,12)])/11
            
            writer.writerow({'zone_id': row['zone_id'],
                             # R does not support ISO8601 easily
                             #'timepoint': timepoint.isoformat(),
                             'timepoint': timepoint.strftime("%Y-%m-%d %H:%M:%S"),
                             'load': row['load'],
                             'temp': temp
                             })

