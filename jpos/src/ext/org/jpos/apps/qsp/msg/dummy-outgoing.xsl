<?xml version="1.0"?> 
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

 <!-- 
      $Id$

      Reply 0800 messages with a 0810 response

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

 <xsl:template match="field[@id='0' and substring(@value,3,1)='0']">
  <field value="{substring(@value,1,2)}10" id="0" />
 </xsl:template>

 <xsl:template match="field[@id='0' and substring(@value,3,1)='2']">
  <field value="{substring(@value,1,2)}30" id="0" />
 </xsl:template>

</xsl:transform>

