#!/bin/bash

# Script para parar todo o sistema Arboviroses
echo "🛑 Parando sistema Arboviroses..."

# Parar todos os containers
docker-compose -f docker-compose.full.yml down

echo "✅ Sistema parado com sucesso!"

# Opção para limpar volumes (descomente se necessário)
# echo "🧹 Removendo volumes (dados serão perdidos)..."
# docker-compose -f docker-compose.full.yml down -v
