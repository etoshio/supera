INSERT INTO modules (id, code, name, description, active) VALUES
 (1, 'PORTAL_COLABORADOR', 'Portal do Colaborador', 'Portal do Colaborador', true),
 (2, 'RELATORIOS_GERENCIAIS', 'Relatórios Gerenciais', 'Relatórios Gerenciais', true),
 (3, 'GESTAO_FINANCEIRA', 'Gestão Financeira', 'Gestão Financeira', true),
 (4, 'APROVADOR_FINANCEIRO', 'Aprovador Financeiro', 'Aprovador Financeiro', true),
 (5, 'SOLICITANTE_FINANCEIRO', 'Solicitante Financeiro', 'Solicitante Financeiro', true),
 (6, 'ADMINISTRADOR_RH', 'Administrador RH', 'Administrador RH', true),
 (7, 'COLABORADOR_RH', 'Colaborador RH', 'Colaborador RH', true),
 (8, 'GESTAO_ESTOQUE', 'Gestão de Estoque', 'Gestão de Estoque', true),
 (9, 'COMPRAS', 'Compras', 'Compras', true),
 (10, 'AUDITORIA', 'Auditoria', 'Auditoria', true)
;

-- allowed departments
INSERT INTO module_allowed_departments (module_id, department) VALUES
 (1, 'TI'), (1, 'FINANCEIRO'), (1, 'RH'), (1, 'OPERACOES'), (1, 'OUTROS'),
 (2, 'TI'), (2, 'FINANCEIRO'), (2, 'RH'), (2, 'OPERACOES'), (2, 'OUTROS'),
 (3, 'TI'), (3, 'FINANCEIRO'),
 (4, 'TI'), (4, 'FINANCEIRO'),
 (5, 'TI'), (5, 'FINANCEIRO'),
 (6, 'TI'), (6, 'RH'),
 (7, 'TI'), (7, 'RH'),
 (8, 'TI'), (8, 'OPERACOES'),
 (9, 'TI'), (9, 'OPERACOES'),
 (10, 'TI')
;

-- incompatibilidades
INSERT INTO module_incompatible (module_id, incompatible_module_id) VALUES
 (4, 5),
 (5, 4),
 (6, 7),
 (7, 6);
