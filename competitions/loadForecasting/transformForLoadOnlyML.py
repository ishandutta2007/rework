import sys, csv, os.path, datetime, string

dataDir = os.path.expanduser('~/rework/competitions/loadForecasting/data/')

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
                                                        ])
        writer.writeheader()
        for row in reader:
            for hour in range(1,25):
                load = row['h'+str(hour)].replace(',','')
            
                if(0 == len(load)):
                    print("Skipping empty load value")
                    continue
                
                timepoint = datetime.datetime(int(row['year']),
                                              int(row['month']),
                                              int(row['day']),
                                              hour-1,
                                              30)
                writer.writerow(
                    {
                        'load': load,
                        'zone_id': row['zone_id'],
                        'day_of_year': timepoint.strftime ("%j"),
                        'hour_of_day': timepoint.strftime ("%H"),
                        'year': timepoint.strftime ("%Y")
                        }
                    )
