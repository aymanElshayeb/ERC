# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn org.apache.poi.**
-keep class android.device.* {*;}
-keep class org.apache.shiro.** { *; }
-keepnames class org.apache.shiro.** { *; }
-keep public class com.app.smartpos.** { *; }
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn javax.imageio.spi.ImageInputStreamSpi
-dontwarn javax.imageio.spi.ImageOutputStreamSpi
-dontwarn javax.imageio.spi.ImageReaderSpi
-dontwarn javax.imageio.spi.ImageWriterSpi
-dontwarn java.awt.Color
-dontwarn javax.servlet.Filter
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.RenderedImage
-dontwarn java.beans.XMLDecoder
-dontwarn java.beans.XMLEncoder
-dontwarn javax.imageio.ImageIO
-dontwarn javax.naming.AuthenticationException
-dontwarn javax.naming.AuthenticationNotSupportedException
-dontwarn javax.naming.Context
-dontwarn javax.naming.InitialContext
-dontwarn javax.naming.NameNotFoundException
-dontwarn javax.naming.NamingEnumeration
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.directory.SearchControls
-dontwarn javax.naming.directory.SearchResult
-dontwarn javax.naming.ldap.Control
-dontwarn javax.naming.ldap.InitialLdapContext
-dontwarn javax.naming.ldap.LdapContext
-dontwarn javax.servlet.FilterChain
-dontwarn javax.servlet.FilterConfig
-dontwarn javax.servlet.ServletContext
-dontwarn javax.servlet.ServletContextEvent
-dontwarn javax.servlet.ServletContextListener
-dontwarn javax.servlet.ServletException
-dontwarn javax.servlet.ServletRequest
-dontwarn javax.servlet.ServletResponse
-dontwarn javax.servlet.http.Cookie
-dontwarn javax.servlet.http.HttpServletRequest
-dontwarn javax.servlet.http.HttpServletRequestWrapper
-dontwarn javax.servlet.http.HttpServletResponse
-dontwarn javax.servlet.http.HttpServletResponseWrapper
-dontwarn javax.servlet.http.HttpSession
-dontwarn javax.servlet.http.HttpSessionBindingEvent
-dontwarn javax.servlet.http.HttpSessionBindingListener
-dontwarn javax.servlet.http.HttpSessionContext
-dontwarn javax.servlet.jsp.JspException
-dontwarn javax.servlet.jsp.JspTagException
-dontwarn javax.servlet.jsp.JspWriter
-dontwarn javax.servlet.jsp.PageContext
-dontwarn javax.servlet.jsp.tagext.TagSupport
-dontwarn org.apache.commons.beanutils.BeanIntrospector
-dontwarn org.apache.commons.beanutils.BeanUtilsBean
-dontwarn org.apache.commons.beanutils.PropertyUtilsBean
-dontwarn org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector
-dontwarn org.apache.commons.configuration2.interpol.ConfigurationInterpolator
-dontwarn org.apache.commons.configuration2.interpol.ConstantLookup
-dontwarn org.apache.commons.configuration2.interpol.EnvironmentLookup
-dontwarn org.apache.commons.configuration2.interpol.Lookup
-dontwarn org.apache.commons.configuration2.interpol.SystemPropertiesLookup