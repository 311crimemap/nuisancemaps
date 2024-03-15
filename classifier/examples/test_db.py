import psycopg2

# Connect to your PostgreSQL database
conn = psycopg2.connect(
    dbname="nuisancemaps",
    user="postgres",
    password="admin",
    host="db",
    port="5432"
)

# Create a cursor object
cur = conn.cursor()

# Execute a SQL query
cur.execute("SELECT distinct category FROM data_311")

# Fetch all rows from the result set
rows = cur.fetchall()

# Print the rows
for row in rows:
    print(row[0])

# Close the cursor and connection
cur.close()
conn.close()

print("DONE")
