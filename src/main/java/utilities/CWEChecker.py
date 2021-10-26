#!/usr/bin/env python
#
# MIT License
# Copyright (c) 2019 Montana State University Software Engineering Labs
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

# -*- coding: utf-8 -*-

"""
Utilizing code from the cvedb.py file from Intel's cve-bin-tool
"""
import sqlite3
import sys


from cve_bin_tool.cvedb import  CVEDB


class CVEtoCWE:
    """
    This class is for querying the cve-bin-tool for the CWE associated with a CVE.
    """

    def getCWEs(self):
        cwes = []
        cveBinTooldb = CVEDB()
        years = cveBinTooldb.nvd_years()
        print(years)
        for year in years:
            cve_data = cveBinTooldb.load_nvd_year(year)
            for cve_item in cve_data["CVE_Items"]:
                try:
                    if "REJECT" in cve_item["cve"]["description"]["description_data"][0]["value"]:
                        continue
                    else:
                        cwe = cve_item["cve"]["problemtype"]["problemtype_data"][0]["description"][0]["value"]
                        cwes.append(cwe)
                except:
                    print(cve_item)

        return cwes



a = CVEtoCWE()
cwes = a.getCWEs()
print(len(cwes))
cweset = set(cwes)
print(len(cweset))
      
for x in cweset:
    print(x)
    print(" ")

    


