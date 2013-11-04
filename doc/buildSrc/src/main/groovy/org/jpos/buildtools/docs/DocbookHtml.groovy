package org.jpos.buildtools.docs

import javax.xml.transform.Transformer

class DocbookHtml extends Docbook
{
    @Override
    protected void preTransform(Transformer transformer, File sourceFile, File outputFile)
    {
        String rootFilename = outputFile.getName();
        rootFilename = rootFilename.substring(0, rootFilename.lastIndexOf('.'));
        transformer.setParameter("root.filename", rootFilename);
        transformer.setParameter("base.dir", outputFile.getParent() + File.separator);
    }
}
