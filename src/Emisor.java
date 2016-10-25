
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
 * @author Tomas Salgado
 */
public class Emisor {
    public static void main(String [] args) throws Exception{
        File outFile      = new File("ArchivoSalida.txt");
        File keyStoreFile = new File("almacenDeLLaves.jks");//almacen de llaves receptor
        String password   = ("seguridad123");//storepass
        String entradaTeclado = "";//string que contendra la contraseña AES ingresada por teclado
        String texto="";//string que contendra el texto a encriptar 
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //1. Generar una llave de sesión para encriptar con AES un archivo de texto de largo arbitrario.
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");//creamos un objeto KeyGenerator con el algoritmo AES 
        keyGenerator.init(128);//Inicializamos este generador de claves con tamaño de clave de 128bits.
        Key key = keyGenerator.generateKey();//generamos una clave secreta "key" con el metodo generateKey del objeto  KeyGenerator
        System.out.println("Porfavor ingrese su clave de 16 digitos para encriptar el texto:");
        Scanner entradaEscaner = new Scanner (System.in); //Creación de un objeto Scanner
        entradaTeclado = entradaEscaner.nextLine ();
        while(entradaTeclado.length()!=16){//validamos que la contraseña sea de 16 digitos
            System.out.println("Porfavor ingrese una contraseña de 16 digitos");
            entradaTeclado = entradaEscaner.nextLine ();
        }
        key = new SecretKeySpec(entradaTeclado.getBytes(),0, 16, "AES");                
        System.out.println("Contraseña creada correctamente!!\n");
        System.out.println("Ingrese el texto a encriptar:");
        texto=entradaEscaner.nextLine();              
        Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");// Se obtiene un cifrador AES        
        aes.init(Cipher.ENCRYPT_MODE, key);// Se inicializa para encriptacion 
        byte[] encriptado = aes.doFinal(texto.getBytes());//encriptamos el texto que debemos pasar como bytes
                      
        ///////////////////////////////////////////////////////////////////////////////////////////
        //2.generacion del hash del mensaje usando el algoritmo MD5
        ///////////////////////////////////////////////////////////////////////////////////////////
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(texto.getBytes());//le pasamos nuestro texto al objeto md con el método update(). 
        byte[] digest = md.digest();//el método digest() Nos lo devolverá como un array de bytes
        
        /* Se escribe byte a byte en hexadecimal
        System.out.println("\nHash del mensaje:");
        for (byte b : digest) {
         System.out.print(Integer.toHexString(0xFF & b));
        }*/
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
	PrivateKey privatekey = (PrivateKey) myKeyStore.getKey("mykey", "password".toCharArray()); 
	        
	Cipher rsaCipher = Cipher.getInstance("RSA");// Inicializa el Objeto Cipher RSA
	rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);	
	byte[] encodedKey = rsaCipher.doFinal(key.getEncoded());//Encriptar llave simetrica AES con la llave publica RSA    
        String nuew="haihaia";
        Cipher rsa2Cipher = Cipher.getInstance("RSA");// Inicializa el Objeto Cipher RSA
	rsa2Cipher.init(Cipher.ENCRYPT_MODE, privatekey);	
	//byte[] encodedKey = rsa2Cipher.doFinal(key.getEncoded());
           
        
        /*
        rsaCipher.init(Cipher.DECRYPT_MODE, privatekey);//incializamos el objeto rsa
        byte[] wii = rsaCipher.doFinal(encodedKey);
        String deses = new String(wii);
          System.out.println("key:"+deses);*/

	System.out.println("\nAbriendo archivo a escribir: "+outFile);
	FileOutputStream outToFile = new FileOutputStream(outFile);

	System.out.println("Escribiendo Datos");	
	outToFile.write(encodedKey);//Escribir llave AES encriptada al archivo.
	// Escribir el texto plano encriptado al archivo.
	outToFile.write(encriptado);
	System.out.println("Cerrando Archivos");
	outToFile.close();
    }    
}
