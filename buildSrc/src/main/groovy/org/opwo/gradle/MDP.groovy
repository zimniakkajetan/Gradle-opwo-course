import java.security.DigestInputStream
import java.security.MessageDigest

class MDP {

   String getMDP(File file) {
      file.withInputStream {
        new DigestInputStream(it, MessageDigest.getInstance('MD5')).withStream {
           it.eachByte {}
           it.messageDigest.digest().encodeHex() as String
         }
      }
   }
}
