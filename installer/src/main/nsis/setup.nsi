Name Tangara
BrandingText "$(BRANDING_TEXT)"

# Directories
#!define FILES_DIR "${NSISDIR}\..\tmp\tangara"
#!define DIST_DIR "${NSISDIR}\..\dist"
#!define RESOURCES_DIR "${NSISDIR}\..\resources"

# General Symbol Definitions
!define COMPANY Colombbus
!define URL www.colombbus.org
!define REGKEY "SOFTWARE\${COMPANY}\$(^Name)"
!define REGKEY_LAUNCH "${COMPANY}.$(^Name).1"
!define VERSION ${VERSION_NUMBER}
!define EXTENSION ".tgr"

# MUI Symbol Definitions
!define MUI_ICON "${RESOURCES_DIR}\tangara.ico"
!define MUI_UNICON "${RESOURCES_DIR}\tangara_uninstall.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_LICENSEPAGE_CHECKBOX
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_REGISTRY_KEY ${REGKEY}
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULTFOLDER Tangara
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_ABORTWARNING
!define MUI_LANGDLL_REGISTRY_ROOT HKLM
!define MUI_LANGDLL_REGISTRY_KEY ${REGKEY}
!define MUI_LANGDLL_REGISTRY_VALUENAME InstallerLanguage
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "${RESOURCES_DIR}\header.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "${RESOURCES_DIR}\header.bmp"
!define MUI_FINISHPAGE_LINK http://tangara.colombbus.org
!define MUI_FINISHPAGE_LINK_LOCATION http://tangara.colombbus.org
# Ne marche pas ??
!define MUI_WELCOMEFINISHPAGE_BITMAP "${RESOURCES_DIR}\side.bmp"

# Included files
!include Sections.nsh
!include MUI2.nsh

# Reserved Files
!insertmacro MUI_RESERVEFILE_LANGDLL

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_LICENSE "$(LICENSE_TEXT)"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
# Uninstaller pages
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE English ;first language is the default language
!insertmacro MUI_LANGUAGE French
!insertmacro MUI_LANGUAGE Spanish

# License texts
LicenseLangString LICENSE_TEXT ${LANG_ENGLISH} "${RESOURCES_DIR}\license-en.txt"
LicenseLangString LICENSE_TEXT ${LANG_FRENCH}  "${RESOURCES_DIR}\license-fr.txt"
LicenseLangString LICENSE_TEXT ${LANG_SPANISH} "${RESOURCES_DIR}\license-es.txt"

# Texts
LangString FINISH_TITLE ${LANG_ENGLISH} "Setup completed"
LangString FINISH_TITLE ${LANG_FRENCH} "Installation terminée"
LangString FINISH_TITLE ${LANG_SPANISH} "Instalación finalizada"
LangString UNINSTALL_LINK ${LANG_ENGLISH} "Uninstall Tangara"
LangString UNINSTALL_LINK ${LANG_FRENCH} "Désinstaller Tangara"
LangString UNINSTALL_LINK ${LANG_SPANISH} "Desinstalar Tangara"
LangString FINISH_CONTENT ${LANG_ENGLISH} "To start Tangara, click on the Start menu -> Tangara.$\r$\nYou can also click on the shortcut Tangara on the desktop.$\r$\nFor more information, you can go on our website : http://tangara.colombbus.org"
LangString FINISH_CONTENT ${LANG_FRENCH} "Pour lancer Tangara, cliquez sur Tangara dans le menu Démarrer -> Tangara.$\r$\nVous pouvez aussi cliquer sur le raccourci Tangara sur le bureau.$\r$\nPour d'autres informations, vous pouvez aller sur notre site http://tangara.colombbus.org"
LangString FINISH_CONTENT ${LANG_SPANISH} "Para iniciar Tangara, clic en el menú Inicio -> Tangara.$\r$\nUsted tambi�n puede hacer clic en el atajo Tangara en el escritorio.$\r$\nSi desea obtener más informaciones, usted puede ir a nuestro sitio web http://tangara.colombbus.org"
LangString FINISH_BUTTON ${LANG_ENGLISH} "Finish"
LangString FINISH_BUTTON ${LANG_FRENCH} "Terminer"
LangString FINISH_BUTTON ${LANG_SPANISH} "Terminar"
LangString BRANDING_TEXT ${LANG_ENGLISH} "Tangara Setup"
LangString BRANDING_TEXT ${LANG_FRENCH} "Installation de Tangara"
LangString BRANDING_TEXT ${LANG_SPANISH} "Instalación de Tangara"
LangString LICENSE_TOP ${LANG_ENGLISH} "Please read and accept the following license:"
LangString LICENSE_TOP ${LANG_FRENCH} "Merci de lire et d'accepter le texte ci-dessous :"
LangString LICENSE_TOP ${LANG_SPANISH} "Para instalar Tangara, debe leer y aceptar las condiciones de la licencia:"
LangString START_MENU_TOP ${LANG_ENGLISH} "Select the Start Menu folder in which you would like to create the program's shortcuts:"
LangString START_MENU_TOP ${LANG_FRENCH} "Choisissez le répertoire du Menu Démarrer dans lequel vous souhaitez mettre le programme :"
LangString START_MENU_TOP ${LANG_SPANISH} "Seleccione la carpeta del menú Inicio en el que desea poner el programa:"

!define MUI_FINISHPAGE_TITLE "$(FINISH_TITLE)"
!define MUI_FINISHPAGE_TEXT "$(FINISH_CONTENT)"
!define MUI_FINISHPAGE_BUTTON "$(FINISH_BUTTON)"
!define MUI_LICENSEPAGE_TEXT_TOP "$(LICENSE_TOP)"
!define MUI_STARTMENUPAGE_TEXT_TOP "$(START_MENU_TOP)"


# Installer attributes
OutFile "${DIST_DIR}\tangara_${VERSION_NUMBER}.exe"
InstallDir $PROGRAMFILES\Tangara
CRCCheck on
XPStyle on
ShowInstDetails show
VIProductVersion "${VERSION_NUMBER}.0"
VIAddVersionKey /LANG=${LANG_ENGLISH} ProductName Tangara
VIAddVersionKey /LANG=${LANG_ENGLISH} ProductVersion "${VERSION_NUMBER}"
VIAddVersionKey /LANG=${LANG_ENGLISH} CompanyName "${COMPANY}"
VIAddVersionKey /LANG=${LANG_ENGLISH} CompanyWebsite "${URL}"
VIAddVersionKey /LANG=${LANG_ENGLISH} FileVersion "${VERSION_NUMBER}"
VIAddVersionKey /LANG=${LANG_ENGLISH} FileDescription ""
VIAddVersionKey /LANG=${LANG_ENGLISH} LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# Functions
Function "JVM"
ClearErrors
ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"

         ${Switch} $LANGUAGE
         ${Case} ${LANG_ENGLISH}
         IfErrors 0 NoAbortEn
         MessageBox MB_YESNO|MB_ICONQUESTION "Couldn't find a Java Development Kit installed. Before installing you must install the java environment : See www.java.com. Do you wish to continue ?" IDYES +5
         Abort
         NoAbortEn:
         MessageBox MB_YESNO|MB_ICONQUESTION "This will install $(^Name). Do you wish to continue?" IDYES +2
         Abort
         ${Break}
         ${Case} ${LANG_SPANISH}
          IfErrors 0 NoAbortEs
          MessageBox MB_YESNO|MB_ICONQUESTION "No se ha podido encontrar un kit de desarrollo de Java instalada. Antes de instalar debe instalar el entorno Java: www.java.com Ver. ¿Desea continuar?" IDYES +5
          Abort
          NoAbortEs:
          MessageBox MB_YESNO|MB_ICONQUESTION "Con esta acción se instalará $(^Name). ¿Desea continuar?" IDYES +2
          Abort
         ${Break}
         ${Case} ${LANG_FRENCH}
         IfErrors 0 NoAbortFr
         MessageBox MB_YESNO|MB_ICONQUESTION "Il n'y a aucun environnement JAVA sur votre ordinateur. Avant d'installer Tangara, il est recommandé d'en installer un (que vous pouvez trouver sur www.java.com). Etes vous sûr de vouloir continuer ?" IDYES +5
         Abort
         NoAbortFr:
         MessageBox MB_YESNO|MB_ICONQUESTION "Ceci installera $(^Name). Voulez-vous continuer ?" IDYES +2
         Abort
         ${Break}
         ${EndSwitch}
FunctionEnd

Function .onInit
   InitPluginsDir
   !insertmacro MUI_LANGDLL_DISPLAY
   Call "JVM"
FunctionEnd

Function un.onInit
    !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuGroup
    !insertmacro MUI_LANGDLL_DISPLAY
    ${Switch} $LANGUAGE
        ${Case} ${LANG_ENGLISH}
            MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
            Abort
        ${Break}
        ${Case} ${LANG_SPANISH}
            MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "¿Estás seguro de querer eliminar completamente $(^Name) y todos sus componentes?" IDYES +2
            Abort
        ${Break}
        ${Case} ${LANG_FRENCH}
            MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Etes-vous sûr de vouloir désinstaller complètement $(^Name) et tous ses composants?" IDYES +2
            Abort
        ${Break}
    ${EndSwitch}
FunctionEnd

Function un.onUninstSuccess
  HideWindow
  ${Switch} $LANGUAGE
         ${Case} ${LANG_ENGLISH}
         MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
         ${Break}
         ${Case} ${LANG_SPANISH}
         MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) se ha eliminado de su ordenador."
         ${Break}
         ${Case} ${LANG_FRENCH}
         MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) a été enlevé de votre ordinateur avec succès."
         ${Break}
         ${EndSwitch}
FunctionEnd

# Installer Sections
Section Files
	# Copy any existing .properties files
	SetOutPath $TEMP\tangara_installer
	CopyFiles /SILENT /FILESONLY $INSTDIR\*.properties $TEMP\tangara_installer

	# Remove previous files
    RMDir /r /REBOOTOK $INSTDIR

    # Write program files
    SetOutPath $INSTDIR
    SetOverwrite on
    File /r "${FILES_DIR}\*"

	# Copy previously backuped properties files
	CopyFiles /SILENT /FILESONLY $TEMP\tangara_installer\*.properties $INSTDIR

	# Remove temp directory
    RMDir /r /REBOOTOK $TEMP\tangara_installer

    # Write uninstaller
    WriteUninstaller "$INSTDIR\uninstall.exe"
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    WriteRegStr HKLM "${REGKEY}\Components" Files 1
SectionEnd

Section Registry
    WriteRegStr HKCR "${EXTENSION}" "" "${REGKEY_LAUNCH}"
    WriteRegStr HKCR "${REGKEY_LAUNCH}" "" "$(^Name)"
    WriteRegStr HKCR "${REGKEY_LAUNCH}\shell\open\command" "" '"$INSTDIR\tangara.exe" "%1"'
    WriteRegStr HKCU "${REGKEY}" Version ${VERSION_NUMBER}
    WriteRegStr HKLM "${REGKEY}\Components" Registry 1
SectionEnd

Section Shortcuts
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\$(^Name).lnk" $INSTDIR\tangara.exe
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\$(UNINSTALL_LINK).lnk" $INSTDIR\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_END
    CreateDirectory "$DESKTOP"
    CreateShortCut "$DESKTOP\Tangara.lnk" "$INSTDIR\tangara.exe"
    #CreateDirectory "{userappdata}\Microsoft\Internet Explorer\Quick Launch"
    #CreateShortCut "{userappdata}\Microsoft\Internet Explorer\Quick Launch\Tangara.lnk" "$INSTDIR\tangara.exe"
    WriteRegStr HKLM "${REGKEY}\Components" Shortcuts 1
SectionEnd

Section post
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" Publisher "${COMPANY}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" URLInfoAbout "${URL}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
    System::Call "shell32.dll::SHChangeNotify(l, l, i, i) v (0x08000000, 0, 0, 0)"
SectionEnd

#### Uninstaller sections ####

Section un.Files
    RmDir /r /REBOOTOK $INSTDIR
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegValue HKLM "${REGKEY}\Components" Files
SectionEnd

Section un.Registry
    DeleteRegKey HKCR "${EXTENSION}"
    DeleteRegKey HKCR "${REGKEY_LAUNCH}"
    DeleteRegKey HKCU "${REGKEY}"
    DeleteRegValue HKLM "${REGKEY}\Components" Registry
SectionEnd

Section un.Shortcuts
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\$(^Name).lnk"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\$(UNINSTALL_LINK).lnk"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    #RMDir "{userappdata}\Microsoft\Internet Explorer\Quick Launch"
    #RMDir "$DESKTOP"
    DeleteRegValue HKLM "${REGKEY}\Components" Shortcuts
SectionEnd

Section un.post
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    System::Call "shell32.dll::SHChangeNotify(l, l, i, i) v (0x08000000, 0, 0, 0)"
    Push $R0
    StrCpy $R0 $StartMenuGroup 1
    StrCmp $R0 ">" no_smgroup
no_smgroup:
    Pop $R0
SectionEnd