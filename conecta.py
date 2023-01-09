import mysql.connector
from mysql.connector import errorcode
import sys
import socket

#try:
db_connection = mysql.connector.connect(host='localhost', user='root', password='mysqlonos',database='teste')
#cursor = db_connection.cursor()
print("Conexão estabelecida com sucesso")
#except mysql.connector.Error as error:
#    if error.errno == errorcode.ER_BAD_DB_ERROR:
#        print("Base de dados inexistente");
#    elif error.errno == errorcode.ER.ACCESS_DENIED_ERROR:
#        print("Erro no login")
#    else:
#        print(error)
#else:
#    db_connection.close()

# Variável que manipula as coisas do BD
cursor = db_connection.cursor()
# Comandos do Socket Server
HOST = ''
PORTA = 8347

# Abre o Socket Server
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

orig = (HOST, PORTA)
server.bind(orig)
server.listen(1)
print("Ouvindo...");

while(True):
    # Conexão com o cliente
    conexao, cliente = server.accept()
    print("Conectado por ",cliente)

    # Recebe mensagem do cliente
    msg = conexao.recv(1024)
    print("Mensagem recebida: ",msg.decode("utf-8"))
    idQuery = msg.decode("utf-8")

    # Busca no BD
    sql = "SELECT * FROM trusted WHERE IPPorta LIKE \'"+idQuery+"\'"
    print(sql)
    cursor.execute(sql);

    for (id,IPPorta,score) in cursor:
        print(id,IPPorta,score)
        scoreRes = score

    # Envia dados para o cliente
    conexao.send(str(scoreRes).encode("utf-8"))

    # Encerra variáveis de conexão com Socket e BD
    conexao.shutdown(socket.SHUT_RDWR)
    conexao.close()
    print("Conexão com o cliente finalizada")

cursor.close()
db_connection.commit()
db_connection.close()
