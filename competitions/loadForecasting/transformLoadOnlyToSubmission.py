import sys, csv, os.path, datetime, string

dataDir = os.path.expanduser('~/rework/competitions/loadForecasting/data/')
id = 1
summaryRow = None

# TODO upgrade to argparse if I ever reuse this script
with open(dataDir + sys.argv[1], 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)
    
    with open(dataDir + sys.argv[2],'wb') as csvoutfile:
        writer = csv.DictWriter(csvoutfile, fieldnames=['id',
                                                        'zone_id',
                                                        'year',
                                                        'month',
                                                        'day',
                                                        'h1',
                                                        'h2',
                                                        'h3',
                                                        'h4',
                                                        'h5',
                                                        'h6',
                                                        'h7',
                                                        'h8',
                                                        'h9',
                                                        'h10',
                                                        'h11',
                                                        'h12',
                                                        'h13',
                                                        'h14',
                                                        'h15',
                                                        'h16',
                                                        'h17',
                                                        'h18',
                                                        'h19',
                                                        'h20',
                                                        'h21',
                                                        'h22',
                                                        'h23',
                                                        'h24'
                                                        ])
        writer.writeheader()
        for row in reader:

            timepoint = datetime.datetime.strptime(row['year']+row['day_of_year'],
                                                   "%Y%j")
            
            if(None == summaryRow):
                # create dictionary for summary row
                summaryRow = {
                    'zone_id': '21',
                    'year': timepoint.strftime ("%Y"),
                    'month': timepoint.strftime ("%m").lstrip('0'),
                    'day': timepoint.strftime ("%d").lstrip('0'),
                    }
                for i in range(1,25):
                    summaryRow['h'+str(i)] = 0

            hour = 'h' +  str(int(row['hour_of_day']) + 1)
            summaryRow[hour] += int(row['load'])
            
            if('h1' == hour):
                newRow = {
                    'zone_id': row['zone_id'],
                    'year': timepoint.strftime ("%Y"),
                    'month': timepoint.strftime ("%m").lstrip('0'),
                    'day': timepoint.strftime ("%d").lstrip('0'),
                    }

            newRow[hour] = row['load']

            if('h24' == hour):
                newRow['id'] = id
                writer.writerow(newRow)
                id += 1
                if('20' == row['zone_id']):
                    # write out summary of all loads for that day
                    summaryRow['id'] = id
                    writer.writerow(summaryRow)
                    id += 1
                    summaryRow = None
