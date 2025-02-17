# SPD-License-Identifier: BSD-3-Clause

SUMMARY = "Community Logitech Media Server control application"
HOMEPAGE = "https://github.com/ralph-irving/jivelite"
SECTION = "multimedia"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3801e66dff470211e7f52eed22094635"

DEPENDS = " \
    libsdl \
    libsdl-ttf \
    libsdl-image \
    libsdl-gfx \
    expat \
    luajit \
"

SRCREV = "39db5722a3bc8a9b924d1a8bba39328bf649df66"

SRC_URI = "git://github.com/ralph-irving/${PN}.git;protocol=https;branch=master \
           file://0001-fix-jivelite-cross-compile-issue.patch;patch=1 \
           file://jivelite.service \
           file://DroidSansFallback.ttf \
"

S = "${WORKDIR}/git"

inherit useradd systemd

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "--system --home-dir /home/jive --create-home --user-group jive"

EXTRA_OEMAKE = 'PREFIX="${STAGING_EXECPREFIXDIR}"'

# Overwrite it to 1 for using CJK font.
JIVE_USE_CJK_FONT ?= "0"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/bin/${PN} ${D}${bindir}/

    install -d ${D}${libdir}
    cp -r ${B}/lib/* ${D}${libdir}/

    install -d ${D}${datadir}
    cp -r ${B}/share/* ${D}${datadir}/

    if [ "${JIVE_USE_CJK_FONT}" = "1" ]; then
        # Use Andriod font to replace default font used by jivelite, which is
        # capable of display Chinese, Japanese and Korean characters correctly.
        # (http://wiki.slimdevices.com/index.php/Adding_Chinese,_Japanese,_Korean,_etc._Fonts_to_Your_Controller)
        mv ${D}${datadir}/jive/fonts/FreeSans.ttf ${D}${datadir}/jive/fonts/FreeSans.ttf.origin
        mv ${D}${datadir}/jive/fonts/FreeSansBold.ttf ${D}${datadir}/jive/fonts/FreeSansBold.ttf.origin
        cp ${WORKDIR}/DroidSansFallback.ttf ${D}${datadir}/jive/fonts/
        cd ${D}${datadir}/jive/fonts/
        ln -s ./DroidSansFallback.ttf FreeSans.ttf
        ln -s ./DroidSansFallback.ttf FreeSansBold.ttf
    fi

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/${PN}.service ${D}${systemd_system_unitdir}/${PN}.service

    chown    jive:jive ${D}${bindir}/${PN}
    chown -R jive:jive ${D}${libdir}/lua
    chown -R jive:jive ${D}${datadir}/jive
    chown -R jive:jive ${D}${datadir}/lua

}

FILES:${PN} = " \
    ${bindir}/${PN} \
    ${libdir}/* \
    ${datadir}/* \
    ${systemd_system_unitdir}/${PN}.service \
"

SYSTEMD_SERVICE:${PN} = "${PN}.service"

# need more effort to make the Makefile to work properly, walk around now
CLEANBROKEN = "1"
INSANE_SKIP:${PN} = "ldflags"
INSANE_SKIP:${PN}-dev = "ldflags"
