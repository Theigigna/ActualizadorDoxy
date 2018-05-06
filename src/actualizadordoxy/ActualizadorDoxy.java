/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package actualizadordoxy;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.jdom2.Document;         // |
import org.jdom2.Element;          // |\ Librerías
import org.jdom2.JDOMException;    // |/ JDOM
import org.jdom2.input.SAXBuilder; // |
/**
 *
 * @author iribera
 * @version 1.0 11/04/2018
 */
public class ActualizadorDoxy {

    private String rutaProyecto;
    private String rutaDoc;

    public ActualizadorDoxy(String[] rutas) {
        //asignamos los argumentos del constructor a las variables de la clase
        this.rutaProyecto = rutas[0];
        this.rutaDoc = rutas[1];
        //Se añade una carpeta project donde se guardarán los cpp
        crearDirectorio(rutaDoc + "\\Project");
        //borramos todos los archivos 
        borrarDirectorio(rutaDoc + "\\Project");
        //volvemos a crear la carpeta que hemos borrado
        crearDirectorio(rutaDoc + "\\Project");
        //limpiamos el logger previo
        File file = new File(this.rutaDoc + "\\loggerDoxy.txt");
        boolean borrado =  file.delete();
        if(borrado){
            System.out.println("logger borrado");
        }
    }
    
    
    
    /**
     * @param args the command line arguments. Serán: [0] la ruta del proyecto a documentar, [1] la ruta donde se guardarán los documentos generados
     */
    public void ejecutar() {
        
        String ruta;
        String rutaDocumentacion;
        if("".equals(this.rutaProyecto) || "".equals(this.rutaDoc))
        {
            JOptionPane.showMessageDialog(null, "Debe introducir las rutas del proyecto y de destino de la documentación.", "Error en la rutas", JOptionPane.WARNING_MESSAGE);
        }else{
            
            //ruta es la ruta donde esta el projecto con los archivos (xml, ctl, ...)
            ruta = this.rutaProyecto;
            //ruta documentacion será donde quieras que se guarden los cpp generados (ejecutará el doxy teniendo en cuenta la ruta del proyecto, lo suyo es que la de documentacion también esté dentro)
            rutaDocumentacion = this.rutaDoc + "\\Project";
            //se llama al navegarCarpeta, que generará los cpp
            navegarCarpeta(ruta, rutaDocumentacion);
            JOptionPane.showMessageDialog(null, "CPP actualizados con exito.", "Actualización terminada.", JOptionPane.INFORMATION_MESSAGE);
            //organizará los cpp por carpetas (solo commons y elementos)
            organizarCarpetas(rutaDocumentacion);
            JOptionPane.showMessageDialog(null, "CPP ordenados con exito.", "Actualización terminada.", JOptionPane.INFORMATION_MESSAGE);
            //generará los grupos de los cpp
            navegarCarpetasGrupos(rutaDocumentacion, rutaDocumentacion);
            crearCpp(this.rutaDoc, "DoxyfileConfig", "*  @defgroup Project");
            JOptionPane.showMessageDialog(null, "Grupos actualizados con exito.", "Actualización terminada.", JOptionPane.INFORMATION_MESSAGE);
            //ejecuta el doxy (tiene que estar ya configurado)
            ejecutarDoxy(ruta);
            JOptionPane.showMessageDialog(null, "Doxygen ejecutado", "Actualización terminada.", JOptionPane.INFORMATION_MESSAGE);
        }        
    }
    
    /**
     * 
     * @param ruta Ruta en la que va a inspeccionar los archivos
     * @param rutaGuardado Ruta donde guardar los posibles .cpp que se vayan a generar
     */
    private void navegarCarpeta(String ruta, String rutaGuardado){
        //Obtenemos el archivo de la ruta actual
        File archivo = new File(ruta);
        //Recorremos todos los archivos que contenga esa ruta
        for (File ficheroEntrada : archivo.listFiles()) {
            //Primero comprobamos si es un directorio o no
            if (ficheroEntrada.isDirectory()) {
                //En el caso de ser un directorio, volvemos a llamar a navegarCarpeta con la ruta
                navegarCarpeta(ficheroEntrada.getPath(), rutaGuardado + "\\" + ficheroEntrada.getName());
            } else {
                //En el caso de ser un archivo, miraremos si es un .xml
                if(ficheroEntrada.getName().endsWith(".xml"))
                {
                    //En el caso de ser un xml, llamaremos a la funcion traducirXml que se encargara de crear los directorios y el .cpp correspondiente
                    traducirXml(ficheroEntrada.getPath(), rutaGuardado);
                }else{
                    //si no es un xml, comprobamos si es un ctl
                    if(ficheroEntrada.getName().endsWith(".ctl"))
                    {
                        //En el caso de ser un ctl, llamaremos a la funcion traducirCtl que se encargara de crear los directorios y el .cpp correspondiente
                        traducirCtl(ficheroEntrada.getPath(), rutaGuardado);
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @param ruta Ruta del archivo xml a documentar
     * @param rutaGuardado Ruta de guardado para los .cpp correspondientes
     */
    private void traducirXml(String ruta, String rutaGuardado){
        // TODO code application logic here
        //Se crea un SAXBuilder para poder parsear el archivo
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File( ruta );
        String rutaTesting = "C:\\Users\\iribera\\Desktop\\Dame archivos loco";
        try
        {
            //Se crea el documento a traves del archivo
            Document document = (Document) builder.build( xmlFile );
            //Se obtiene la raiz 'panel'
            Element rootNode = document.getRootElement();
            //Se obtiene la lista de hijos de la raiz 'tables'
            List<Element> listaEvents = rootNode.getChildren( "events" );
            List<Element> listaShapes = rootNode.getChildren( "shapes" );
            String nombreElemento = xmlFile.getName().replaceAll(".xml", "");
            String nombrePrimeraSeccion;
            //Se recorre la lista de hijos 'events' (aunque sea 1)
            for(Element evento: listaEvents){  
                List<Element> listaScripts = evento.getChildren( "script" );
                for(Element script: listaScripts){
                    //obtiene el script y sustituye los caracteres &quot; por \
                    nombrePrimeraSeccion = script.getAttributeValue( "name" );
                    crearCpp(rutaGuardado , nombreElemento + "_" + nombrePrimeraSeccion, script.getText().replaceAll("&quot;", "\""));
                }
            }
            //Se recorre la lista de hijos 'Shapes' (aunque sea 1)
            for(Element shapes: listaShapes){  
                List<Element> listaShape = shapes.getChildren( "shape" );
                //Se recorre la lista de hijos 'shape'
                for(Element shape: listaShape){
                    nombrePrimeraSeccion = shape.getAttributeValue("Name");
                    List<Element> listaShapeEvents = shape.getChildren( "events" );
                    //Se recorre la lista de hijos 'events'
                    for(Element eventShape: listaShapeEvents){
                        List<Element> listaScripts = eventShape.getChildren( "script" );
                        String nombreSegundaSeccion;
                        for(Element script: listaScripts){
                            //obtiene el script y sustituye los caracteres &quot; por \
                            nombreSegundaSeccion = script.getAttributeValue( "name" );
                            crearCpp(rutaGuardado , nombreElemento + "_" + nombrePrimeraSeccion + "_" + nombreSegundaSeccion, script.getText().replaceAll("&quot;", "\""));
                        }
                    }
                }
            }
        }catch ( IOException | JDOMException io ) {
            actualizarLog("La funcion traducirXml ha fallado en: " + ruta);
        }
    }
    
    /**
     * 
     * @param ruta Ruta del archivo ctl a documentar
     * @param rutaGuardado Ruta de guardado para los .cpp correspondientes
     */
    private void traducirCtl(String ruta, String rutaGuardado){
        try {
            crearDirectorio(rutaGuardado);
            //archivo de origen
            File origenFile = new File( ruta );
            //ruta del archivo de origen
            Path origenPath = Paths.get( ruta );
            //ruta del archivo de destino (copiarlo y cambiar extension)
            Path destinoPath = Paths.get( rutaGuardado + "\\" + origenFile.getName().replaceFirst(".ctl", ".cpp") );
            //sobreescribir el fichero de destino si existe y lo copia
            Files.copy(origenPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (FileNotFoundException ex) {
            actualizarLog("La funcion traducirCtl ha fallado en: " + ruta + ", el archivo no existe.");
        } catch (IOException ex) {
            actualizarLog("La funcion traducirCtl ha fallado en: " + ruta + ", error al copiar el archivo.");
        }
    }
    
    /**
     * 
     * @param ruta Ruta de la carpeta donde se quiere guardar el .cpp generado
     * @param nombre Nombre que se quiere para el .cpp
     * @param texto Contenido que se quiere en el archivo
     */
    private void crearCpp(String ruta, String nombre, String texto){
        
        //Creamos el directorio para el archivo
        crearDirectorio(ruta);
        //Creamos el archivo, añadiendo el nombre del propio archivo y la extensión .cpp (con la que queremos crearlo) a la ruta existente
        File archivo = new File(ruta + "\\" + nombre + ".cpp");
        //Declaramos el buffer de escritura
        BufferedWriter bw = null;
        try
        {
            //Abrimos el buffer the escritura para el archivo (sin usar el contructor con append = true, para sobreescribir el archivo)
            bw = new BufferedWriter(new FileWriter(archivo));
            //Sobreescribimos el archivo con el codigo actualizado
            bw.write(texto);
        }catch(IOException io){
            //No se ha podido crear el archivo
            actualizarLog("Algo ha fallado escribiendo el archivo: " + nombre + ".cpp");
        }finally{
            //Siempre hay que cerrar el buffer (si se ha llegado a crear)
            if(bw != null)
            {
                try {
                    //Si se ha creado, cerramos el buffer
                    bw.close();
                } catch (IOException ex) {
                    actualizarLog("Fallo al cerrar el buffer (crearCpp): " + ruta);
                }
            }
        }
    }
    
    /**
     * 
     * @param ruta La ruta del directorio a crear, solo se llamara si se va a crear un cpp (llamada a crearCpp [en traducirXml o traducirCtl])
     */
    private void crearDirectorio(String ruta){
        //Creamos el archivo con la ruta obtenida
        File directorio = new File(ruta);
        //Variable para ver si se ha creado el directorio o ya estaba
        boolean comprobacion;
        //Si no existe el directorio lo creara
        if (!directorio.exists()) {
            //Crea el directorio (incluyendo todos los directorios previos que tampoco existian
            comprobacion = directorio.mkdirs();
            //Comprobación de que ha funcionado
            if(!comprobacion){
                actualizarLog("no se ha podido crear el directorio: " + ruta + ".");
            }
        }        
    }
    
    /**
     * 
     * @param ruta
     * @param rutaGuardado 
     */
    private void navegarCarpetasGrupos(String ruta, String rutaGuardado){
        //Funcion que genera los .cpp con los grupos del doxygen
        //Texto que se generaá con los grupos
        String texto = "";
        //Obtenemos el archivo de la ruta actual
        File archivo = new File(ruta);
        //Recorremos todos los archivos que contenga esa ruta
        for (File ficheroEntrada : archivo.listFiles()) {
            //Primero comprobamos si es un directorio o no
            if (ficheroEntrada.isDirectory()) {
                //System.out.println("Nombre fichero: " + ficheroEntrada.getName());
                //En el caso de ser un directorio, volvemos a llamar a navegarCarpeta con la ruta
                navegarCarpetasGrupos(ficheroEntrada.getPath(), rutaGuardado + "\\" + ficheroEntrada.getName());
                texto += "\t*  @defgroup " + ficheroEntrada.getName() + "\n" + "\t*  @ingroup " + archivo.getName() + "\n";
            }else{
                //Se busca si hay algún cpp, si hay, se creará un txt en la carpeta con la jerarquia de grupos
                if (ficheroEntrada.getName().endsWith(".cpp")) {
                    texto += "\t*  @defgroup " + ficheroEntrada.getName().replaceAll(".cpp", "") + "\n" + "\t*  @ingroup " + archivo.getName() + "\n";
                }
            }
        }
        File archivoGrupos = new File(ruta + "\\grupos" + archivo.getName() + ".cpp");
        //Declaramos el buffer de escritura
        BufferedWriter bw = null;
        try
        {
            //Abrimos el buffer the escritura para el archivo (sin usar el contructor con append = true, para sobreescribir el archivo)
            bw = new BufferedWriter(new FileWriter(archivoGrupos));
            //Sobreescribimos el archivo con el codigo actualizado
            bw.write(texto);
        }catch(IOException e){
            actualizarLog("Algo ha fallado escribiendo el archivo: grupos" + archivo.getName() + ".txt");
        }finally{
            //Siempre hay que cerrar el buffer (si se ha llegado a crear)
            if(bw != null)
            {
                //Si se ha creado, cerramos el buffer
                try {
                    bw.close();
                } catch (IOException ex) {
                    actualizarLog("Fallo al cerrar el buffer.");
                }
            }
        }
    }
    
    /**
     * 
     * @param ruta carpeta que organizará
     */
    private void organizarCarpetas(String ruta){
        //Funcion que buscara las carpetas donde haya elementos o comunes y los ordenara en sub carpetas
        
        //Obtenemos el archivo de la ruta actual
        File archivo = new File(ruta);
        List<File> listaCpp = new ArrayList<>();
        
        //Recorremos todos los archivos que contenga esa ruta
        for (File ficheroEntrada : archivo.listFiles()) {
            if (ficheroEntrada.isDirectory()) {
                //si es un directorio, se llama de manera recursiva
                organizarCarpetas(ruta + "\\" + ficheroEntrada.getName());
            }else{
                //Se busca si hay algún cpp, si hay, se creará un txt en la carpeta con la jerarquia de grupos
                if (ficheroEntrada.getName().endsWith(".cpp")) {
                    //al ser un cpp, lo añade a la lista
                    listaCpp.add(ficheroEntrada);
                }
            }
        }
        
        int numeroArchivos = listaCpp.size();
        
        if(numeroArchivos > 0){
            //intentará organizarlo
            for(int i = 0; i < numeroArchivos; i++){
                //buscara el tipo de archivo en la lista de cpp
                String nombreSplit [] = listaCpp.get(i).getName().split("_"); //sacas las partes del nombre
                if(nombreSplit.length >= 3){
                    //ira viendo si alguno de los archivos es de este tipo y en cuanto uno lo sea, ordenara toda la carpeta
                    switch (nombreSplit[0]){
                        case "COMM":
                            clasificarArchivos(ruta, 0);
                            i = numeroArchivos;
                            //System.out.println("A ordenar : " + ruta + "\\" + listaCpp.get(i).getName());
                            //una vez encuentra uno ordenable, no hace falta seguir buscando mas, llamara a clasificar archivos
                            break;
                        default:
                            switch (nombreSplit[1]){
                                case "CMD":
                                case "ELM":
                                case "ALM":
                                case "INF":
                                    clasificarArchivos(ruta, 0); //se podría hacer en nivel 2 asumiento que son elementos y están divididos correctamente en carpetas, pero por si incluimos diferentes en una carpeta
                                    i = numeroArchivos;                                    
                                    //System.out.println("A ordenar : " + ruta + "\\" + listaCpp.get(i).getName());
                                    //una vez encuentra uno ordenable, no hace falta seguir buscando mas, llamara a clasificar archivos
                                    break;
                                default:
                                    //System.out.println("No hay que ordenar en la carpeta: " + ruta);
                                    break;
                            }
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @param ruta Ruta de archivos actual donde queremos ordenar
     * @param nivel Numero desde el 0 al 2 (3 niveles)
     */
    private void clasificarArchivos(String ruta, int nivel){
        //la idea es coger todos los archivos de la ruta que se da y organizarlos dependiendo del nivel dado
        //se miraran todos los archivos que cuando se haga spli("_") tengan longitud de 3 o mas (lo llamaremos solo desde carpetas con comm en el primer nivel
        //o con alm, cmd, inf, elm en el segundo nivel
        //Obtenemos el archivo de la ruta actual
        File archivo = new File(ruta);
        List <ArrayList<String>> archivos = new ArrayList<ArrayList<String>>(); //Lista de las listas de archivos diferenciados por identificador
        List <String> identificadoresArchivo = new ArrayList <>();
        
        //si el nivel es mas de 3, no queremos ordenarlos
        if(nivel > 2){
            return;
        }
        
        //Recorremos todos los archivos que contenga esa ruta
        for (File ficheroEntrada : archivo.listFiles()) {
            //comprobamos si es un directorio
            if (ficheroEntrada.isDirectory()) {
                //organizarCarpetas(ficheroEntrada.getPath());
            }else{
                //Se busca si hay algún cpp, si hay, se creará un txt en la carpeta con la jerarquia de grupos
                String [] nombreArchivo = ficheroEntrada.getName().split("_");
                //si son archivos "ordenables" (cpps con la estructura x...x_y...y_z...z.cpp)
                if (nombreArchivo.length >= 3 && ficheroEntrada.getName().contains(".cpp")) {
                    //comprobamos si ya existe una lista para el identificador por el que estamos ordenando
                    String nuevoId = "";
                    for(int i = 0; i <= nivel; i++){
                        nuevoId += nombreArchivo[i];
                        if(i != nivel){
                            nuevoId += "_";
                        }
                    }
                    if(identificadoresArchivo.contains(nuevoId)){
                        //si existe, obtenemos el indice e introducimos la ruta del archivo en la lista correspondiente
                        int indice = identificadoresArchivo.indexOf(nuevoId);
                        archivos.get(indice).add( ruta + "\\" + ficheroEntrada.getName());
                    }else{
                        //sino, creamos una nueva lista para ese identificados
                        ArrayList nuevaLista = new ArrayList<>();
                        nuevaLista.add(ruta + "\\" + ficheroEntrada.getName());
                        //añadimos el nuevo identificador
                        identificadoresArchivo.add(nuevoId);
                        //añadimos la lista con el nuevo elemento
                        archivos.add(nuevaLista);
                    }
                }
            }
        }
        
        if(identificadoresArchivo.size() <= 1 )
        {
            //si ya están ordenados por el identificador actual, se pasa al siguiente
            clasificarArchivos(ruta, nivel + 1);
        }else{
            //hacemos la division por carpetas
            for(int i = 0; i < identificadoresArchivo.size(); i++){
                //se crea una carpeta para ese identificador
                String nuevaRuta = ruta + "\\" + identificadoresArchivo.get(i); //ruta para la nueva carpeta
                crearDirectorio(nuevaRuta); //carpeta con el nombre del identificador
                moverArchivos(archivos.get(i), nuevaRuta, nivel + 1); //llamamos a mover archivos
            }
        }
    }
    
    /**
     * 
     * @param rutasArchivos lista con las rutas de los archivos a mover
     * @param ruta ruta donde se han de mover los archivos
     * @param siguienteNivel siguiente nivel para la ordenación
     */
    private void moverArchivos(ArrayList <String> rutasArchivos, String ruta, int siguienteNivel){
        //recorre el array con las rutas y va moviendo los archivos a la nueva ruta
        for(int i = rutasArchivos.size() - 1; i >= 0; i--)
        {
            File archivoAMover = new File(rutasArchivos.get(i));
            File rutaDestino = new File(ruta, archivoAMover.getName());
            archivoAMover.renameTo(rutaDestino);
        }
        //si el siguiente nivel es ordenable, vuelve a llamar a clasigicarArchivos con ese nivel y la ruta nueva donde se han metido los archivos
        if(siguienteNivel <= 2){
            clasificarArchivos(ruta, siguienteNivel);
        }
    }
    
    private void borrarDirectorio(String ruta){
        //Ira borrando todos los archivos de un directorio
        //Obtenemos el archivo de la ruta actual
        File archivo = new File(ruta);
        //Recorremos todos los archivos que contenga esa ruta
        int numeroArchivos = archivo.listFiles().length;
        for (int i = numeroArchivos - 1; i >= 0; i--) {
            if(archivo.listFiles()[i].isDirectory()){
                borrarDirectorio(ruta + "\\" + archivo.listFiles()[i].getName());
            }
            boolean borrado = archivo.listFiles()[i].delete();
            if(borrado){
                System.out.println("Archivo borrado con exito.");
            }else{
                System.out.println("El archivo " + archivo.listFiles()[i].getName() + " no ha podido ser borrado.");
            }
        }
    }
    
    /**
     * 
     * @param ruta ejecuta el comando de doxygen para la generación de la documentacion
     */
    private void ejecutarDoxy(String ruta){
        //Obtenemos el archivo de la ruta actual
        File archivo = new File(ruta);
        //Recorremos todos los archivos que contenga esa ruta
        //buscando la ruta donde este el Doxyfile
        for (File ficheroEntrada : archivo.listFiles()) {
            //si ve un directorio, se llama de manera recursiva
            if (ficheroEntrada.isDirectory()) {
                ejecutarDoxy(ruta + "\\" + ficheroEntrada.getName());
            }else{
                //si no es directorio, busca si es el Doxyfile
                if ("Doxyfile".equals(ficheroEntrada.getName())) {
                    //ejecuta el comando: doxygen DoxyFile
                    try {
                        Runtime.getRuntime().exec("doxygen " + ruta + "\\Doxyfile");
                    } catch (IOException ioe) {
                        actualizarLog("Fallo al ejecutar doxygen");
                    }
                }
            }   
        }
    }
    
    /**
     * 
     * @param texto Teto a añadir al log
     */
    private void actualizarLog(String texto){
        //Creamos el directorio para el archivo
        crearDirectorio(this.rutaDoc);
        //Creamos el archivo, añadiendo el nombre del propio archivo y la extensión .cpp (con la que queremos crearlo) a la ruta existente
        File archivo = new File(this.rutaDoc + "\\loggerDoxy.txt");
        //Declaramos el buffer de escritura
        BufferedWriter bw = null;
        try
        {
            //Abrimos el buffer the escritura para el archivo (sin usar el contructor con append = true, para sobreescribir el archivo)
            bw = new BufferedWriter(new FileWriter(archivo));
            //Sobreescribimos el archivo con el codigo actualizado
            bw.append(texto + "\n");
        }catch(IOException io){
            //No se ha podido crear el archivo
            System.out.println("io exception en actualizar log");
        }finally{
            //Siempre hay que cerrar el buffer (si se ha llegado a crear)
            if(bw != null)
            {
                try {
                    //Si se ha creado, cerramos el buffer
                    bw.close();
                } catch (IOException ex) {
                    actualizarLog("Fallo al cerrar los streams del actualizarLog.");
                }
            }
        }
    }    
}
