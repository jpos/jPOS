<?xml version="1.0"?> 
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

 <!-- 
      $Id$

      Copy field 11 to field 37 for demonstration purposes
 -->

 <xsl:template match="/isomsg">
  <isomsg>
   <xsl:apply-templates />
  </isomsg>
 </xsl:template>

 <!-- handle inner isomsgs -->
 <xsl:template match="isomsg">
  <isomsg id="{@id}">
   <xsl:apply-templates />
  </isomsg>
 </xsl:template>

 <xsl:template match="field">
  <field>
    <xsl:attribute name="id">
     <xsl:value-of select="@id" />
    </xsl:attribute>
    <xsl:attribute name="value">
     <xsl:value-of select="@value" />
    </xsl:attribute>
   <xsl:if test="@type">
    <xsl:attribute name="type">
     <xsl:value-of select="@type" />
    </xsl:attribute>
   </xsl:if>
   <xsl:apply-templates/>
  </field>
 </xsl:template>

 <xsl:template match="field[@id='11']">
  <xsl:copy-of select="." />
  <field value="{@value}" id="37" />
 </xsl:template>

</xsl:transform>

