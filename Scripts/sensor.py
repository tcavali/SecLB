import mysql.connector
from mysql.connector import errorcode
import sys
import socket

#try:
db_connection = mysql.connector.connect(host='localhost', user='root', password='mysqlonos',database='teste')
print("Conexão estabelecida com sucesso")

# Variável que manipula as coisas do BD
cursor = db_connection.cursor()
# Comandos do Socket Client
HOST = 'localhost'
PORTA = 12345

if len(sys.argv) != 2:
    db_connection.close()
    sys.exit(1)

ip_porta = sys.argv[1]

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORTA))
    
    # Busca no BD
    # sql = "SELECT * FROM trusted WHERE IPPorta LIKE \'10.0.0.1:9622\'"
    # print(sql)
    sql = "SELECT * FROM trusted WHERE IPPorta LIKE %s"
    cursor.execute(sql, (ip_porta,));

    for (id,IPPorta,score) in cursor:
        print(id,IPPorta,score)
        ip, porta = IPPorta.split(":")
        scoreRes = f"{ip}|{porta}|{score}"
        print(scoreRes)

    # Envia dados para o cliente
    s.send(str(scoreRes).encode("utf-8"))

    print("Conexão com o cliente finalizada")

db_connection.close()
