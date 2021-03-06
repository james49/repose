package org.openrepose.commons.utils.transform.xslt;

import org.openrepose.commons.utils.pooling.ResourceContextException;
import org.openrepose.commons.utils.pooling.SimpleResourceContext;
import org.openrepose.commons.utils.transform.StreamTransform;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author zinic
 */
public class StreamToXsltTransform extends AbstractXslTransform implements StreamTransform<InputStream, OutputStream> {

   public StreamToXsltTransform(Templates transformationTemplates) {
      super(transformationTemplates);
   }

   @Override
   public void transform(final InputStream source, final OutputStream target) {
      getXslTransformerPool().use(new SimpleResourceContext<Transformer>() {

         @Override
         public void perform(Transformer resource) throws ResourceContextException {
            try {
               resource.transform(new StreamSource(source), new StreamResult(target));
            } catch (TransformerException te) {
               throw new XsltTransformationException("Failed while attempting XSLT transformation;. Reason: "
                       + te.getMessage(), te);
            }
         }
      });
   }
}
