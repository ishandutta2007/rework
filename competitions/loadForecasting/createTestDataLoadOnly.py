import csv, os.path, datetime, string

# From http://www.kaggle.com/c/global-energy-forecasting-competition-2012-load-forecasting
# "Given actual temperature history, the 8 weeks below in the load history are set to be missing and are required to be backcasted. It's OK to use the entire history to backcast these 8 weeks.

# 2005/3/6 - 2005/3/12;

# 2005/6/20 - 2005/6/26;

# 2005/9/10 - 2005/9/16;

# 2005/12/25 - 2005/12/31;

# 2006/2/13 - 2006/2/19;

# 2006/5/25 - 2006/5/31;

# 2006/8/2 - 2006/8/8;

# 2006/11/22 - 2006/11/28;

# In addition, the particpants need to forecast hourly loads from 2008/7/1 to 2008/7/7. No actual temperatures are given for this week."

# Note: many of these prediction dates are over holidays such as Memorial Day, 4th of July, Christmas, and New Year's Eve


dataDir = os.path.expanduser('~/rework/competitions/loadForecasting/data/')

with open(dataDir + 'predictionDates.csv', 'rb') as csvinfile:
    reader = csv.DictReader(csvinfile)
    
    with open(dataDir + 'testData_for_LoadOnly.csv','wb') as csvoutfile:
        writer = csv.DictWriter(csvoutfile, fieldnames=['load',
                                                        'zone_id',
                                                        'day_of_year',
                                                        'hour_of_day',
                                                        'year',
                                                        ])
        writer.writeheader()
        for row in reader:
            for zone in range(1,21):
                for hour in range(1,25):
                    load = -1 # flag that we must predict the value
            
                    timepoint = datetime.datetime(int(row['year']),
                                                  int(row['month']),
                                                  int(row['day']),
                                                  hour-1,
                                                  30)
                    writer.writerow(
                        {
                            'load': load,
                            'zone_id': zone,
                            'day_of_year': timepoint.strftime ("%j"),
                            'hour_of_day': timepoint.strftime ("%H"),
                            'year': timepoint.strftime ("%Y")
                            }
                        )
