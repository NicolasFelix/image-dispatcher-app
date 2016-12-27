;This file will be executed next to the application bundle image
;I.e. current directory will contain folder ExchangeApp with application files
[Setup]
AppId={{com.digitech.city.exchange}}
AppName=ExchangeApp
AppVersion=1.0
AppVerName=ExchangeApp 1.0
AppPublisher=Digitech S.A.
AppComments=ExchangeApp
AppCopyright=Copyright (C) 2016
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\ExchangeApp
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=Digitech S.A.
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=ExchangeApp
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=ExchangeApp\ExchangeApp.ico
UninstallDisplayIcon={app}\ExchangeApp.ico
UninstallDisplayName=ExchangeApp
WizardImageStretch=No
WizardSmallImageFile=ExchangeApp-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64


[Languages]
Name: "french"; MessagesFile: "compiler:Languages\French.isl"

[Files]
Source: "ExchangeApp\ExchangeApp.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "ExchangeApp\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\ExchangeApp"; Filename: "{app}\ExchangeApp.exe"; IconFilename: "{app}\ExchangeApp.ico"; Check: returnFalse()
Name: "{commondesktop}\ExchangeApp"; Filename: "{app}\ExchangeApp.exe";  IconFilename: "{app}\ExchangeApp.ico"; Check: returnTrue()


[Run]
Filename: "{app}\ExchangeApp.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\ExchangeApp.exe"; Description: "{cm:LaunchProgram,ExchangeApp}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\ExchangeApp.exe"; Parameters: "-install -svcName ""ExchangeApp"" -svcDesc ""ExchangeApp"" -mainExe ""ExchangeApp.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\ExchangeApp.exe "; Parameters: "-uninstall -svcName ExchangeApp -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
