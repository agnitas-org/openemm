-- link: ![admin]%(rdir-domain)/form.action?agnCI=%(company-id)&agnFN=profile&agnUID=##AGNUID## agnPROFILE;
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--                                                                                                                                                                                                                                                                  --
--                                                                                                                                                                                                                                                                  --
--        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   --
--                                                                                                                                                                                                                                                                  --
--        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    --
--        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           --
--        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            --
--                                                                                                                                                                                                                                                                  --
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

function profile (ctx, parm, cust)
	if ctx.anon and parm.anon ~= nil then
		return parm.anon
	end
	
	if ctx._result == nil then
		ctx._result = agn.strmap ('%(rdir_domain)/form.action?agnCI=%(company_id)&agnFN=profile&agnUID=##AGNUID##')
	end
	return ctx._result
end
#<data.company.token () != null>#
-- link: ![admin]%(rdir-domain)/form.action?agnCTOKEN=%(company-token)&agnFN=profile&agnUID=##AGNUID## agnPROFILE;

function profile (ctx, parm, cust)
	if ctx.anon and parm.anon ~= nil then
		return parm.anon
	end
	
	if ctx._result == nil then
		ctx._result = agn.strmap ('%(rdir_domain)/form.action?agnCTOKEN=%(company_token)&agnFN=profile&agnUID=##AGNUID##')
	end
	return ctx._result
end
