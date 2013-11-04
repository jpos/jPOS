package org.jpos.buildtools.docs;

import org.apache.fop.apps.Fop
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.gradle.api.logging.LogLevel

import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource

public class DocbookPdf extends Docbook
{
    @Override
    protected void postTransform(File foFile)
    {
        FopFactory fopFactory = FopFactory.newInstance();

        def baseURL = sourceDirectory.toURL().toExternalForm()
        fopFactory.setBaseURL(baseURL);
        fopFactory.setFontBaseURL(baseURL);

        OutputStream out = null;
        final File pdfFile = getPdfOutputFile(foFile);
        logger.debug("Transforming 'fo' file " + foFile + " to PDF: " + pdfFile);

        try
        {
            out = new BufferedOutputStream(new FileOutputStream(pdfFile));

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF,out);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            Source src = new StreamSource(foFile);
            Result res = new SAXResult(fop.getDefaultHandler());

            switch (project.gradle.startParameter.logLevel)
            {
                case LogLevel.DEBUG:
                case LogLevel.INFO:
                    break;
                default:
                    logging.captureStandardOutput(LogLevel.INFO)
                    logging.captureStandardError(LogLevel.INFO)
            }
            transformer.transform(src, res);
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }

        if (!foFile.delete())
        {
            logger.warn("Failed to delete 'fo' file " + foFile);
        }
    }

    private File getPdfOutputFile(File foFile)
    {
        return new File(foFile.parent, this.project.rootProject.name + '.pdf')
    }
}
