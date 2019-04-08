;This file will be executed next to the application bundle image
;I.e. current directory will contain folder imgDispatcherApp with application files
[Setup]
AppId={{fr.perso.nfelix}}
AppName=ImgDispatcherApp
AppVersion=1.0
AppVerName=ImgDispatcherApp 1.0
AppPublisher=LouMéou
AppComments=ImgDispatcherApp
AppCopyright=Copyright (C) 2019
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\imgDispatcherApp
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=LouMéou Corporation
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=imgDispatcherApp
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=imgDispatcherApp\imgDispatcherApp.ico
UninstallDisplayIcon={app}\imgDispatcherApp.ico
UninstallDisplayName=imgDispatcherApp
WizardImageStretch=No
WizardSmallImageFile=imgDispatcherApp-setup-icon.bmp
ArchitecturesInstallIn64BitMode=x64


[Languages]
Name: "french"; MessagesFile: "compiler:Languages\French.isl"

[Files]
Source: "imgDispatcherApp\imgDispatcherApp.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "imgDispatcherApp\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\imgDispatcherApp"; Filename: "{app}\imgDispatcherApp.exe"; IconFilename: "{app}\imgDispatcherApp.ico"; Check: returnFalse()
Name: "{commondesktop}\imgDispatcherApp"; Filename: "{app}\imgDispatcherApp.exe";  IconFilename: "{app}\imgDispatcherApp.ico"; Check: returnTrue()


[Run]
Filename: "{app}\imgDispatcherApp.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\imgDispatcherApp.exe"; Description: "{cm:LaunchProgram,imgDispatcherApp}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\imgDispatcherApp.exe"; Parameters: "-install -svcName ""imgDispatcherApp"" -svcDesc ""imgDispatcherApp"" -mainExe ""imgDispatcherApp.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\imgDispatcherApp.exe "; Parameters: "-uninstall -svcName imgDispatcherApp -stopOnUninstall"; Check: returnFalse()

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
