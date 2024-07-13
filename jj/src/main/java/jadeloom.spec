 
Name:    jadeloom
Version: 1.0.0
Release: 1
License: GPLv3
Summary:
Summary(ru):
Source: %{name}-%{version}.tar.gz

%description

%description -l ru

%prep
%autosetup -n %{name}-%{version}

%build

%install

%files
%{_libdir}/libJDAst.so
