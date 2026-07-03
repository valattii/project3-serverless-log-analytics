SELECT *
FROM swiftpay_processed_logs
LIMIT 10;

SELECT service_name,
       COUNT(*) as error_count
FROM swiftpay_processed_logs
WHERE level = 'ERROR'
GROUP BY service_name
ORDER BY error_count DESC;

SELECT level,
COUNT(*) as count,
ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM swiftpay_processed_logs
GROUP BY level
ORDER BY count DESC;

select substring(timestamp, 12, 2) as hour,
count(*) as error_count
from swiftpay_processed_logs
where level = 'ERROR'
group by substring(timestamp, 12, 2)
order by hour;

select timestamp,service_name, message
from swiftpay_processed_logs
where level='ERROR'
order by timestamp desc
limit 20;
