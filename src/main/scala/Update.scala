import java.io.File
import org.jdom2.input.SAXBuilder
import org.apache.commons.imaging.Imaging
import com.sun.imageio.plugins.jpeg.JPEGMetadata
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileSystem
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants

object Update extends App {
	
  go
  
  def go() = {
    println("a:)")
    val start = new File("I:\\photo and video\\foto\\canada\\flickr-temp") 
    recurse(start)
  }
  
  def recurse(f : File) {
    val z = f.listFiles()
    for (ff <- z) {
      if (ff.isDirectory()) {
        recurse(ff)
      } else {
        if (ff.getName().endsWith(".jpg")) {
          process(ff)
        }
      }
    }
  }
  
  
  def process(f : File) {
    val z = f.getAbsolutePath().dropRight(4)
    val infoName = z + "-info.xml"
    val commentsName = z + "-comments.xml"
    
    val (title, description) = getInfo(infoName)

    println(title)
    println(description)
    
    update(f, title, description)    
  }
 
  def update(f: File, title : String, description :String) {
    
    
    
//    val img = Imaging.getBufferedImage(f)
        
    val meta = Imaging.getMetadata(f).asInstanceOf[JpegImageMetadata]
    val o = if (meta != null) {
      val d = meta.getExif()
      if (d != null) {
        d.getOutputSet()
      } else {
        new TiffOutputSet
      }
    } else {
      new TiffOutputSet
    }    
    
    val dir = o.getOrCreateRootDirectory()
    dir.removeField(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION)
    dir.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, title)
    
    dir.removeField(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT)
    dir.add(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT,  description)
//    dir.removeField(MicrosoftTagConstants.EXIF_TAG_XPTITLE)
//    dir.add(MicrosoftTagConstants.EXIF_TAG_XPTITLE, title)
    
  
    
    val rw = new ExifRewriter
    val outName = f.getParentFile().getAbsolutePath() + File.separator + "out_" + f.getName()
    val os = new FileOutputStream(outName)
    val os2 = new BufferedOutputStream(os)
    rw.updateExifMetadataLossless(f, os2, o)

    
    val sp = Paths.get(outName)
    val tp = Paths.get(f.getAbsolutePath())
    Files.move(sp, tp, StandardCopyOption.REPLACE_EXISTING)
    
    
    
//    val l = meta.getItems()
//    val i = l.iterator()
//    while (i.hasNext()) {
//      val o = i.next()
//      o.
//      println(o)
//    }
//    
    
    
    println(meta)
  }
  
  def getInfo(infoName : String) = {
    val infoFile = new File(infoName)
    val builder = new SAXBuilder
    val doc = builder.build(infoFile)
    
    val te = doc.getRootElement().getChild("title")
    val title = if (te != null) {
      te.getText();
    } else {
      ""
    }

    val de = doc.getRootElement().getChild("description")
    val desc = if (de != null) {
      de.getText();
    } else {
      ""
    }

    (title, desc)
    
  }  
  
}