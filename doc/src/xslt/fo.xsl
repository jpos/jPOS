<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl d">

    <xsl:import href="http://docbook.sourceforge.net/release/xsl/current/fo/docbook.xsl"/>
    <xsl:import href="http://docbook.sourceforge.net/release/xsl/current/fo/highlight.xsl"/>
    <xsl:import href="asciidoc-fo.xsl"/>
    <xsl:param name="highlight.source" select="1"/>
    <xsl:param name="highlight.xslthl.config">http://docbook.sourceforge.net/release/xsl/current/highlighting/xslthl-config.xml</xsl:param>

    <!-- Font templates -->
    <xsl:template name="pickfont-sans">
      <xsl:text>Arial,sans-serif</xsl:text>
    </xsl:template>
    <xsl:template name="pickfont-serif">
      <xsl:text>Georgia,serif</xsl:text>
    </xsl:template>
    <xsl:template name="pickfont-mono">
      <xsl:text>Liberation Mono,Courier New,Courier,monospace</xsl:text>
    </xsl:template>
    <xsl:template name="pickfont-dingbat">
      <xsl:call-template name="pickfont-sans"/>
    </xsl:template>
    <xsl:template name="pickfont-symbol">
      <xsl:text>Symbol,ZapfDingbats</xsl:text>
    </xsl:template>
    <xsl:template name="pickfont-math">
      <xsl:text>Liberation Serif,Times-Roman</xsl:text>
    </xsl:template>

    <!-- Fonts selection  -->
    <xsl:param name="body.font.family">
      <xsl:call-template name="pickfont-sans"/>
    </xsl:param>
    <xsl:param name="sans.font.family">
      <xsl:call-template name="pickfont-sans"/>
    </xsl:param>
    <xsl:param name="monospace.font.family">
      <xsl:call-template name="pickfont-mono"/>
    </xsl:param>
    <xsl:param name="math.font.family">
      <xsl:call-template name="pickfont-math"/>
    </xsl:param>
    <xsl:param name="title.font.family">
      <xsl:call-template name="pickfont-serif"/>
    </xsl:param>


    <xsl:param name="paper.type" select="'A4'"/>
    <xsl:param name="body.font.master">10</xsl:param>
    <xsl:param name="hyphenate">true</xsl:param>
    <xsl:param name="alignment">justify</xsl:param>
    <xsl:param name="body.font.size">
      <xsl:value-of select="$body.font.master"/><xsl:text>pt</xsl:text>
    </xsl:param>
    <!-- asciidoc-br is customized below
    <xsl:template match="processing-instruction('asciidoc-br')">
      <fo:block/>
    </xsl:template>
    -->
    <xsl:template match="processing-instruction('asciidoc-hr')">
      <fo:block space-after="1em">
        <fo:leader leader-pattern="rule" rule-thickness="0.5pt"  rule-style="solid" leader-length.minimum="100%"/>
      </fo:block>
    </xsl:template>
    <xsl:template match="processing-instruction('asciidoc-pagebreak')">
      <fo:block break-after='page'/>
    </xsl:template>
    <xsl:attribute-set name="monospace.properties">
      <xsl:attribute name="font-size">10pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:param name="symbol.font.family"/>
    <xsl:param name="shade.verbatim">1</xsl:param>
    <xsl:param name="saxon.extensions" select="1"/>
    <xsl:param name="use.extensions" select="'1'"/>
    <xsl:param name="ulink.show" select="1"/>
    <xsl:param name="tablecolumns.extension" select="'1'"/>
    <xsl:param name="linenumbering.extension" select="'1'"/>
    <xsl:param name="linenumbering.everyNth" select="'1'"/>

    <xsl:param name="table.cell.border.color" select="'#000000'"/>
    <xsl:param name="table.cell.border.style" select="'solid'"/>
    <xsl:param name="table.cell.border.thickness" select="'1px'"/>
    <xsl:param name="table.footnote.number.format" select="'a'"/>
    <xsl:param name="table.footnote.number.symbols" select="''"/>
    <xsl:param name="table.frame.border.color" select="'#000000'"/>
    <xsl:param name="table.frame.border.style" select="'solid'"/>
    <xsl:param name="table.frame.border.thickness" select="'1px'"/>

    <xsl:param name="admon.graphics" select="1"/>
    <xsl:param name="admon.graphics.path">images/icons/admon/</xsl:param>
    <xsl:param name="admon.graphics.extension" select="'.svg'"/>
    <xsl:param name="admon.style">
      <xsl:text>margin-left: 0; margin-right: 10%;</xsl:text>
    </xsl:param>
    <xsl:param name="admon.textlabel" select="0"/>

    <xsl:param name="callout.defaultcolumn" select="'60'"/>
    <xsl:param name="callout.graphics.extension" select="'.svg'"/>
    <xsl:param name="callout.graphics" select="'1'"/>
    <xsl:param name="callout.graphics.number.limit" select="'10'"/>
    <xsl:param name="callout.graphics.path" select="'images/icons/callouts/'"/>
    <xsl:param name="callout.list.table" select="'1'"/>

    <xsl:param name="table.borders.with.css" select="1"/>
<!--
    <xsl:param name="section.label.includes.component.label" select="0"/>
    <xsl:param name="generate.section.toc.level" select="0"/>
    <xsl:param name="section.autolabel.max.depth" select="2"/>
    <xsl:param name="section.autolabel" select="0"/>
    <xsl:param name="chapter.autolabel" select="0"/>
-->

    <xsl:template match="d:informalexample[@role='license']">
        <fo:block font-size="9pt">
            <xsl:apply-imports/>
        </fo:block>
    </xsl:template>

    <xsl:template match='xslthl:keyword' mode="xslthl">
        <fo:inline font-weight="normal" color="#AA22FF">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="xslthl:doccomment|xslthl:doctype" mode="xslthl">
        <fo:inline font-weight="normal" color="green">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="xslthl:annotation" mode="xslthl">
        <fo:inline font-weight="normal" color="teal">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="xslthl:string" mode="xslthl">
        <fo:inline font-weight="normal" font-style="italic" color="brown">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="xslthl:directive" mode="xslthl">
        <fo:inline font-weight="normal" font-style="italic" color="blue">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="xslthl:tag" mode="xslthl">
        <fo:inline font-weight="normal" font-style="italic" color="blue">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="xslthl:attribute" mode="xslthl">
        <fo:inline font-weight="normal" color="teal">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="xslthl:value" mode="xslthl">
        <fo:inline font-weight="normal" font-style="italic" color="brown">
            <xsl:apply-templates mode="xslthl"/>
        </fo:inline>
    </xsl:template>

    <xsl:attribute-set name="formal.object.properties">
        <xsl:attribute name="keep-together.within-column">auto</xsl:attribute>
    </xsl:attribute-set>

    <!-- Monospaced fonts are smaller than regular text -->
    <xsl:attribute-set name="monospace.properties">
        <xsl:attribute name="font-size">0.8em</xsl:attribute>
    </xsl:attribute-set>

    <!-- Line Break -->
    <xsl:template match="processing-instruction('br')">
        <fo:block>
            <xsl:text> </xsl:text>
        </fo:block>
    </xsl:template>

    <!--  
         Add page break (see: "docbook", "customization layer")
         www.sagehill.net/docbookxsl/CustomMethods.html#CustomizationLayer
         To enable hard page breaks, add the following template 
	 to your customization layer: (dflc 15/06/2006)
     -->
    <xsl:template match="processing-instruction('hard-pagebreak')">
        <fo:block break-before='page'/>
    </xsl:template>

    <!-- Line break -->
    <xsl:template match="processing-instruction('asciidoc-br')">
        <fo:block>
            <xsl:text> </xsl:text>
        </fo:block>
    </xsl:template>

    <!-- Hard page break -->
    <xsl:template match="processing-instruction('asciidoc-pagebreak')">
        <fo:block break-before='page'/>
    </xsl:template>

    <!--###################################################
                          Programlistings
        ################################################### -->

    <xsl:attribute-set name="verbatim.properties">
        <xsl:attribute name="font-size">8pt</xsl:attribute>
    </xsl:attribute-set>

    <!-- Verbatim text formatting (programlistings) -->
    <xsl:attribute-set name="monospace.verbatim.properties" use-attribute-sets="verbatim.properties">
        <xsl:attribute name="space-before.minimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.maximum">1em</xsl:attribute>
        <xsl:attribute name="space-after.minimum">0.1em</xsl:attribute>
        <xsl:attribute name="space-after.optimum">0.1em</xsl:attribute>
        <xsl:attribute name="space-after.maximum">0.1em</xsl:attribute>
        <xsl:attribute name="border-color">#444444</xsl:attribute>
        <xsl:attribute name="border-style">solid</xsl:attribute>
        <xsl:attribute name="border-width">0.1pt</xsl:attribute>
        <xsl:attribute name="padding-top">0.5em</xsl:attribute>
        <xsl:attribute name="padding-left">0.5em</xsl:attribute>
        <xsl:attribute name="padding-right">0.5em</xsl:attribute>
        <xsl:attribute name="padding-bottom">0.5em</xsl:attribute>
        <xsl:attribute name="margin-left">0.5em</xsl:attribute>
        <xsl:attribute name="margin-right">0.5em</xsl:attribute>
        <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
    </xsl:attribute-set>

    <!-- Shade (background) programlistings -->
    <!-- Only shade programlisting and screen verbatim elements -->
    <xsl:attribute-set name="shade.verbatim.style">
      <xsl:attribute name="background-color">
        <xsl:choose>
          <xsl:when test="self::d:programlisting|self::d:screen">#E0E0E0</xsl:when>
          <xsl:otherwise>#FFFFFF</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:attribute-set>

    <!-- Make links bold -->
    <xsl:attribute-set name="xref.properties">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <!--
        <xsl:attribute name="font-style">italic</xsl:attribute>
        <xsl:attribute name="color">blue</xsl:attribute>
    -->
    </xsl:attribute-set>

    <!-- Prevent blank pages in output -->
    <xsl:template name="book.titlepage.verso"/>
    <xsl:template name="book.titlepage.before.verso"/>
    <xsl:template name="book.titlepage.separator"/>

    <xsl:template name="book.titlepage.recto">
        <fo:block>
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="100%"/>
                <fo:table-body>
                    <fo:table-row>
                        <fo:table-cell text-align="center">
                            <fo:block>
                                <fo:external-graphic src="url(images/logo.png)" content-height="scale-to-fit" height="4.00in"/>
                            </fo:block>
                            <fo:block font-family="Helvetica" font-size="14pt" padding-before="10mm">
                                <xsl:value-of select="/d:book/d:info/d:title"/>
                            </fo:block>
                            <fo:block font-family="Helvetica" font-size="10pt" padding="10mm">
                                Revision: <xsl:value-of select="/d:book/d:info/d:revhistory/d:revision/d:revnumber"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>

    <xsl:param name="generate.toc">
        article toc,title
        book    toc,title,figure,table,example,equation
    </xsl:param>

    <xsl:template name="xref.target">
      <xsl:param name="context" select="."/>
      <xsl:param name="object" select="."/>
      <xsl:text>#</xsl:text>
      <xsl:call-template name="object.id">
          <xsl:with-param name="object" select="$object"/>
      </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
