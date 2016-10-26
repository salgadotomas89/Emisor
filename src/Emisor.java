
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;


/**
 *
 * @author Tomas Salgado, Diego Riquelme
 */
public class Emisor {
    public static void main(String [] args) throws Exception{
        boolean flag = true;
        int opcion;
        File inFile = new File("ArchivoEntrada.txt");
        File outFile      = new File("ArchivoSalida.txt");
        File keyStoreFile = new File("almacenDeLLaves.jks");//almacen de llaves receptor
        String password   = ("seguridad123");//storepass
        String entradaTeclado = "";//string que contendra la contraseña AES ingresada por teclado
        String mensaje;//string que contendra el texto a encriptar 
        BufferedReader entrada = new BufferedReader(new InputStreamReader (System.in));

        System.out.println("Bienvenido!");
        while(flag){
            System.out.println("Seleccione la opcion que desea:");
            System.out.println("1.- Ingresar un mensaje por teclado");
            System.out.println("2.- Tengo un archivo de texto con el mensaje");
            System.out.println("3.- Salir");
            opcion = Integer.parseInt (entrada.readLine());
      
            switch (opcion) {//Variable opcion de tipo entero es la opcion ingresada por el usuario
	        case 1:
	        mensaje = entrada.readLine();
	        break;
	        case 2:	                        		                        	
                try {	                            									
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //1. Generar una llave de sesión para encriptar con AES un archivo de texto de largo arbitrario.
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                System.out.println("Abriendo archivo a leer: "+inFile);
                FileInputStream rawDataFromFile = new FileInputStream(inFile);
                byte[] fileData = new byte[(int) inFile.length()];
                //Se leen los datos
                System.out.println("Leyendo Datos");
                rawDataFromFile.read(fileData);
                //Se transforma a String el arreglo de bytes
                mensaje = new String(fileData, StandardCharsets.UTF_8);
                // Generamos una clave de 128 bits adecuada para AES
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");//aleatorio
                keyGenerator.init(128);
                Key key = keyGenerator.generateKey();
                // Se obtiene un cifrador AES
                Cipher cifradorAes = Cipher.getInstance("AES/ECB/PKCS5Padding");
                // Se inicializa para encriptacion y se encripta el texto,
                // que debemos pasar como bytes.
                cifradorAes.init(Cipher.ENCRYPT_MODE, key);
                byte[] mensajeEncriptado = cifradorAes.doFinal(mensaje.getBytes());
                    
                ///////////////////////////////////////////////////////////////////////////////////////////
                //2.generacion del hash del mensaje usando el algoritmo MD5
                ///////////////////////////////////////////////////////////////////////////////////////////
                MessageDigest md = MessageDigest.getInstance( "MD5" );
                md.update(fileData);
                byte[] digest = md.digest();        
                ////////////////////////////////////////////////////////////////////////////////////////////
                //3.Encripta(RSA) llave de sesión con llave publica del receptor del mensaje
                ////////////////////////////////////////////////////////////////////////////////////////////
                KeyStore myKeyStore = KeyStore.getInstance("JKS");//Carga el keystore
                FileInputStream inStream = new FileInputStream(keyStoreFile);
                myKeyStore.load(inStream, password.toCharArray());
                
                // Lee las llaves privada y publica del keystore.
                Certificate cert = myKeyStore.getCertificate("millave");//le pasamos nuestro alias de nuestro keystore
                PublicKey publicKey = cert.getPublicKey();
                @SuppressWarnings("unused")
                PrivateKey privatekey = (PrivateKey) myKeyStore.getKey("millave", "123456789".toCharArray()); 
	        System.out.println("privada"+privatekey);
                Cipher rsaCipher = Cipher.getInstance("RSA");// Inicializa el Objeto Cipher RSA
                rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);	
                byte[] encodedKey = rsaCipher.doFinal(key.getEncoded());//Encriptar llave simetrica AES con la llave publica RSA    
        
                Cipher cifradorRsaHash = Cipher.getInstance("RSA");
                cifradorRsaHash.init(Cipher.ENCRYPT_MODE, privatekey);
                System.out.println("Holaaa444444444");
                // Encriptar llave simetrica AES con la llave publica RSA
                byte[] hashEncriptado = cifradorRsaHash.doFinal(digest);
        
        
                /*
                rsaCipher.init(Cipher.DECRYPT_MODE, privatekey);//incializamos el objeto rsa
                byte[] wii = rsaCipher.doFinal(encodedKey);
                String deses = new String(wii);
                System.out.println("key:"+deses);*/

                System.out.println("\nAbriendo archivo a escribir: "+outFile);
                FileOutputStream outToFile = new FileOutputStream(outFile);

                System.out.println("Escribiendo Datos");	
                outToFile.write(encodedKey);//Escribir llave AES encriptada al archivo.
                System.out.println("Escribiendo hash encriptado");
                //outToFile.write(hashEncriptado);
                System.out.println("Escribiendo Datos");	
                //outToFile.write(mensajeEncriptado);
                // Escribir el texto plano encriptado al archivo.
                //outToFile.write(encriptado);
                System.out.println("Cerrando Archivos");
                outToFile.close();
                
                }
                catch (Exception e) {
                    /* Si hay algún tipo de error de disco al leer o escribir el archivo
                    * (Por ejemplo, el disco esta sin espacio), entonces se ejecuta el código.
                    */
                    System.out.println("Doh: "+e); 
		}									
	        break;
	        case 3:
	        flag = false;
	        break;
	        default:
	        System.out.println("Debe ingresar una opcion valida");/*Si se ingresa una opcion no valida
	        se vuelve a solicitar y se muestra ese
	        mensaje*/
	        break;
	    }
	}
        
    }    
}
